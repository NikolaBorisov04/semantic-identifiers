package com.github.nikolaborisov04.semanticidentifiers.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe

object ApiKeySettings
{
    private val credentialAttributes = CredentialAttributes(
        generateServiceName("SemanticIdentifiers", "GeminiApiKey")
    )

    var apiKey: String?
        get() = PasswordSafe.instance.getPassword(credentialAttributes)
        set(value) = PasswordSafe.instance.setPassword(credentialAttributes, value)
}
