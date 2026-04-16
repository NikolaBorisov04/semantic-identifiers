package com.github.nikolaborisov04.semanticidentifiers.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class SettingsConfigurable : Configurable {
    private var apiKeyField: JBPasswordField? = null

    // Cache the loaded key so isModified() can check it instantly in memory
    private var initialApiKey: String = ""

    override fun getDisplayName(): String = "Semantic Identifiers"

    override fun createComponent(): JComponent {
        val field = JBPasswordField()
        apiKeyField = field

        return panel {
            row("Gemini API Key:") {
                cell(field)
                    .align(Align.FILL)
                    .comment("Get your free API key from <a href=\"https://aistudio.google.com\">Google AI Studio</a>")
            }
        }.also { reset() } // This still calls reset() safely now
    }

    override fun isModified(): Boolean {
        // No PasswordSafe calls blocking the UI.
        val entered = String(apiKeyField?.password ?: charArrayOf())
        return initialApiKey != entered
    }

    override fun apply() {
        val entered = String(apiKeyField?.password ?: charArrayOf())
        ApiKeySettings.apiKey = entered.ifBlank { null }

        // Update our local cache to match what was just saved
        initialApiKey = entered
    }

    override fun reset() {
        // 1. Jump off the main UI thread to do the slow operation
        ApplicationManager.getApplication().executeOnPooledThread {
            val savedKey = ApiKeySettings.apiKey ?: ""

            // 2. Jump back onto the UI thread to update the text field safely
            ApplicationManager.getApplication().invokeLater {
                initialApiKey = savedKey
                apiKeyField?.text = savedKey
            }
        }
    }

    override fun disposeUIResources() {
        apiKeyField = null
    }
}