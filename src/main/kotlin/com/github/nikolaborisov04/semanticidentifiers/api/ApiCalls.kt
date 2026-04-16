package com.github.nikolaborisov04.semanticidentifiers.api

import java.net.http.HttpClient
import java.time.Duration

// Base class for all AI provider implementations.
// This abstraction allows future developers to easily add new providers (e.g., OpenAI, Claude)
// by implementing the getSuggestions method and providing the necessary configuration checks.
abstract class ApiCalls {
    
    // Unique identifier for the provider (e.g., "gemini", "openai").
    // Used for saving settings and internal logic.
    abstract val id: String

    // The display name of the AI provider (e.g., "Gemini", "OpenAI").
    // Used for UI elements like progress indicators.
    abstract val providerName: String

    // Shared HTTP client configured with standard timeouts for API communication.
    protected val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    // Fetches three semantic name suggestions from the AI provider based on the given context.
    // targetName: The current name of the identifier.
    // codeContext: The surrounding code context (typically ~30 lines).
    // Returns: A formatted string of suggestions, or an error message prefixed with "API Error" or "Exception".
    abstract fun getSuggestions(targetName: String, codeContext: String): String?

    // Checks if the necessary settings (like API keys) are configured for this provider.
    // Returns true if the provider is ready to make calls.
    abstract fun isConfigured(): Boolean

    // Provides a user-friendly error message explaining how to configure this provider.
    abstract fun getConfigurationError(): String

    // Creates the standardized prompt text used for all providers.
    protected fun createPrompt(targetName: String, codeContext: String): String {
        return "You are a professional software engineer. " +
                "Suggest 3 descriptive, semantic names for the semantic identifier named '$targetName' based on the following code context. " +
                "Return ONLY a numbered list of names.\n\nCode Context:\n$codeContext"
    }

    // Helper to escape strings for JSON payloads.
    // Most AI providers use JSON APIs, so this is a shared utility.
    protected fun escapeForJson(text: String): String {
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")
            .replace("\t", "\\t")
    }
}
