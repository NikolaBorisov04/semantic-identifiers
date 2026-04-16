package com.github.nikolaborisov04.semanticidentifiers.logic

import com.github.nikolaborisov04.semanticidentifiers.api.ApiCalls
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
import kotlin.math.max
import kotlin.math.min

// Singleton logic handler for semantic naming suggestions
object NameSuggester
{
    // Retrieve the provider currently selected in the settings.
    private val apiProvider: ApiCalls 
        get() = com.github.nikolaborisov04.semanticidentifiers.api.ApiProviderFactory.getActiveProvider()

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
        // Read PSI data under a read lock (required by IntelliJ 2024.3+)
        val psiData = ApplicationManager.getApplication().runReadAction<Triple<PsiNamedElement?, String, String>> {
            val namedElement = findNamedElement(element)
            if (namedElement != null) {
                Triple(namedElement, namedElement.name ?: "unknown", getContextCode(namedElement))
            } else {
                Triple(null, "", "")
            }
        }

        val namedElement = psiData.first
        val currentName = psiData.second
        val contextCode = psiData.third

        if (namedElement == null)
        {
            Messages.showErrorDialog(
                project,
                "Please place the cursor on a variable or function name.",
                "No Identifier Found"
            )
            return
        }

        if (!apiProvider.isConfigured())
        {
            Messages.showErrorDialog(
                project,
                apiProvider.getConfigurationError(),
                "API Not Configured"
            )
            return
        }

        // Run the API call in a background task
        val taskTitle = "Analyzing code with ${apiProvider.providerName}..."
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, taskTitle, true)
        {
            override fun run(indicator: ProgressIndicator)
            {
                val result = apiProvider.getSuggestions(currentName, contextCode)

                // Switch back to the UI thread for the message box
                ApplicationManager.getApplication().invokeLater {
                    if (result != null)
                    {
                        if (result.startsWith("API Error") || result.startsWith("Exception"))
                        {
                            Messages.showErrorDialog(project, result, "${apiProvider.providerName} API Error")
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
}
