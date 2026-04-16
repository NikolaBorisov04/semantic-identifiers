package com.github.nikolaborisov04.semanticidentifiers.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.ide.util.PropertiesComponent

// Helper to manage secure storage of API keys and persistent configuration
object ApiKeySettings
{
    private const val SELECTED_PROVIDER_KEY = "com.github.nikolaborisov04.semanticidentifiers.selected_provider"
    private const val DEFAULT_PROVIDER = "gemini"

    // The ID of the currently selected AI provider
    var selectedProvider: String
        get() = PropertiesComponent.getInstance().getValue(SELECTED_PROVIDER_KEY, DEFAULT_PROVIDER)
        set(value) = PropertiesComponent.getInstance().setValue(SELECTED_PROVIDER_KEY, value)

    // Securely retrieves the API key for a specific provider
    fun getApiKey(providerId: String): String?
    {
        val attributes = createCredentialAttributes(providerId)
        return PasswordSafe.instance.getPassword(attributes)
    }

    // Securely stores the API key for a specific provider
    fun setApiKey(providerId: String, value: String?)
    {
        val attributes = createCredentialAttributes(providerId)
        PasswordSafe.instance.setPassword(attributes, value)
    }

    // Generates the service name used in the OS credential store
    private fun createCredentialAttributes(providerId: String): CredentialAttributes
    {
        val serviceName = when (providerId) {
            "gemini" -> "GeminiApiKey"
            else -> "${providerId.replaceFirstChar { it.uppercase() }}ApiKey"
        }
        return CredentialAttributes(generateServiceName("SemanticIdentifiers", serviceName))
    }
}
