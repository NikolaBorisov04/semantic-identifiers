package com.github.nikolaborisov04.semanticidentifiers.api

import com.github.nikolaborisov04.semanticidentifiers.settings.ApiKeySettings

// Registry and factory for AI provider implementations.
// Adding a new provider is as simple as adding its class to the providers list.
object ApiProviderFactory {

    // List of all available AI providers.
    private val providers: List<ApiCalls> = listOf(
        GeminiApiCalls()
    )

    // Returns the provider that is currently selected in the plugin settings.
    fun getActiveProvider(): ApiCalls {
        val selectedId = ApiKeySettings.selectedProvider
        return providers.find { it.id == selectedId } ?: providers.first()
    }

    // Returns a list of all supported provider IDs and names for the settings UI.
    fun getAllProviders(): List<ApiCalls> = providers
}
