package com.github.nikolaborisov04.semanticidentifiers.api

import com.github.nikolaborisov04.semanticidentifiers.settings.ApiKeySettings
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

// Implementation of ApiCalls for the Google Gemini AI service.
class GeminiApiCalls : ApiCalls() {

    override val providerName: String = "Gemini"

    private val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    override fun isConfigured(): Boolean {
        return !ApiKeySettings.apiKey.isNullOrBlank()
    }

    override fun getConfigurationError(): String {
        return "No Gemini API key configured.\n\nGo to Settings → Tools → Semantic Identifiers to add your key."
    }

    override fun getSuggestions(targetName: String, codeContext: String): String? {
        val apiKey = ApiKeySettings.apiKey ?: return null

        return try {
            // Use the shared prompt construction logic
            val rawPrompt = createPrompt(targetName, codeContext)
            
            // Use the shared JSON escaping logic
            val escapedPrompt = escapeForJson(rawPrompt)

            val jsonBody = """
                {
                  "contents": [{
                    "parts": [{"text": "$escapedPrompt"}]
                  }]
                }
            """.trimIndent()

            val request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == 200) {
                parseGeminiResponse(response.body())
            } else {
                "API Error ${response.statusCode()}: ${response.body()}"
            }
        } catch (e: Exception) {
            "Exception: ${e.message}"
        }
    }

    // Manual parser to extract text from the JSON response, correctly handling escape sequences
    private fun parseGeminiResponse(json: String): String? {
        val searchKey = "\"text\": \""
        val startIndex = json.indexOf(searchKey)
        if (startIndex == -1) return null

        val actualStart = startIndex + searchKey.length
        val sb = StringBuilder()
        var i = actualStart

        while (i < json.length) {
            when {
                json[i] == '\\' && i + 1 < json.length -> {
                    when (json[i + 1]) {
                        '"' -> { sb.append('"'); i += 2 }
                        'n' -> { sb.append('\n'); i += 2 }
                        't' -> { sb.append('\t'); i += 2 }
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
