package com.github.nikolaborisov04.semanticidentifiers.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class SettingsConfigurable : Configurable
{
    private var apiKeyField: JBPasswordField? = null

    override fun getDisplayName(): String = "Semantic Identifiers"

    override fun createComponent(): JComponent
    {
        val field = JBPasswordField()
        apiKeyField = field

        return panel {
            row("Gemini API Key:") {
                cell(field)
                    .align(Align.FILL)
                    .comment("Get your free API key from <a href=\"https://aistudio.google.com\">Google AI Studio</a>")
            }
        }.also { reset() }
    }

    override fun isModified(): Boolean
    {
        val saved = ApiKeySettings.apiKey ?: ""
        val entered = String(apiKeyField?.password ?: charArrayOf())
        return saved != entered
    }

    override fun apply()
    {
        val entered = String(apiKeyField?.password ?: charArrayOf())
        ApiKeySettings.apiKey = entered.ifBlank { null }
    }

    override fun reset()
    {
        apiKeyField?.text = ApiKeySettings.apiKey ?: ""
    }

    override fun disposeUIResources()
    {
        apiKeyField = null
    }
}
