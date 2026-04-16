package com.github.nikolaborisov04.semanticidentifiers.api

import java.net.http.HttpClient
import java.time.Duration

// Base class for all AI provider implementations.
// This abstraction allows future developers to easily add new providers (e.g., OpenAI, Claude)
// by implementing the getSuggestions method and providing the necessary configuration checks.
abstract class ApiCalls {
    
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
}
