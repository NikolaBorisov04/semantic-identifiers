package com.github.nikolaborisov04.semanticidentifiers.logic

import com.github.nikolaborisov04.semanticidentifiers.settings.ApiKeySettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import kotlin.math.max
import kotlin.math.min

// Singleton logic handler for semantic naming suggestions
object NameSuggester
{
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="

    // Traverses the PSI tree upwards to find an element that can be renamed
    fun findNamedElement(element: PsiElement?): PsiNamedElement?
    {
        var current = element
        while (current != null)
        {
            if (current is PsiNamedElement) return current
            current = current.parent
        }
        return null
    }

    // Extracts surrounding code lines to provide context for the AI
    private fun getContextCode(element: PsiElement): String
    {
        return try
        {
            val file = element.containingFile ?: return ""
            val project = element.project
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return file.text

            val offset = element.textOffset
            val lineNumber = document.getLineNumber(offset)

            // Capture 15 lines above and below for context
            val startLine = max(0, lineNumber - 15)
            val endLine = min(document.lineCount - 1, lineNumber + 15)

            val startOffset = document.getLineStartOffset(startLine)
            val endOffset = document.getLineEndOffset(endLine)

            document.getText(TextRange(startOffset, endOffset))
        }
        catch (e: Exception)
        {
            element.containingFile?.text ?: ""
        }
    }

    // Entry point for the Action and Intention
    fun suggestAndShow(project: Project, element: PsiElement?)
    {
        val namedElement = findNamedElement(element)

        if (namedElement == null)
        {
            Messages.showErrorDialog(
                project,
                "Please place the cursor on a variable or function name.",
                "No Identifier Found"
            )
            return
        }

        val apiKey = ApiKeySettings.apiKey?.takeIf { it.isNotBlank() }
        if (apiKey == null)
        {
            Messages.showErrorDialog(
                project,
                "No Gemini API key configured.\n\nGo to Settings → Tools → Semantic Identifiers to add your key.",
                "API Key Not Configured"
            )
            return
        }

        val currentName = namedElement.name ?: "unknown"
        val contextCode = getContextCode(namedElement)

        // Run the API call in a background task
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Analyzing code with Gemini...", true)
        {
            override fun run(indicator: ProgressIndicator)
            {
                val result = callGeminiAPI(currentName, contextCode, apiKey)

                // Switch back to the UI thread for the message box
                ApplicationManager.getApplication().invokeLater {
                    if (result != null)
                    {
                        if (result.startsWith("API Error") || result.startsWith("Exception"))
                        {
                            Messages.showErrorDialog(project, result, "Gemini API Error")
                        }
                        else
                        {
                            Messages.showInfoMessage(
                                project,
                                "AI Suggestions for '$currentName':\n\n$result",
                                "Semantic Identifiers"
                            )
                        }
                    }
                    else
                    {
                        Messages.showErrorDialog(project, "Failed to connect to the AI service.", "Connection Error")
                    }
                }
            }
        })
    }

    // Handles the network communication with Gemini
    private fun callGeminiAPI(targetName: String, codeContext: String, apiKey: String): String?
    {
        return try
        {
            val client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build()

            // Escape special characters to maintain valid JSON
            val escapedContext = codeContext
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", "\\t")

            val prompt = "You are a professional software engineer. " +
                    "Suggest 3 descriptive, semantic names for the variable/function named '$targetName' based on the following code context. " +
                    "Return ONLY a numbered list of names.\\n\\nCode Context:\\n$escapedContext"

            val jsonBody = """
                {
                  "contents": [{
                    "parts": [{"text": "$prompt"}]
                  }]
                }
            """.trimIndent()

            val encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8.toString())
            val request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + encodedKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == 200)
            {
                parseGeminiResponse(response.body())
            }
            else
            {
                "API Error ${response.statusCode()}: ${response.body()}"
            }
        }
        catch (e: Exception)
        {
            "Exception: ${e.message}"
        }
    }

    // Manual parser to extract text from the JSON response, correctly handling escape sequences
    private fun parseGeminiResponse(json: String): String?
    {
        val searchKey = "\"text\": \""
        val startIndex = json.indexOf(searchKey)
        if (startIndex == -1) return null

        val actualStart = startIndex + searchKey.length
        val sb = StringBuilder()
        var i = actualStart

        while (i < json.length)
        {
            when
            {
                json[i] == '\\' && i + 1 < json.length ->
                {
                    when (json[i + 1])
                    {
                        '"'  -> { sb.append('"');  i += 2 }
                        'n'  -> { sb.append('\n'); i += 2 }
                        't'  -> { sb.append('\t'); i += 2 }
                        '\\' -> { sb.append('\\'); i += 2 }
                        else -> { sb.append(json[i + 1]); i += 2 }
                    }
                }
                json[i] == '"' -> break
                else -> { sb.append(json[i]); i++ }
            }
        }

        return sb.toString().trim().ifEmpty { null }
    }
}