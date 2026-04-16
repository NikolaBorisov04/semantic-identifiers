package com.github.nikolaborisov04.semanticidentifiers.settings

import com.github.nikolaborisov04.semanticidentifiers.api.ApiProviderFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import javax.swing.DefaultComboBoxModel

class SettingsConfigurable : Configurable {
    private var providerComboBox: ComboBox<String>? = null
    private var apiKeyField: JBPasswordField? = null

    // Cache to check if settings have been modified
    private var initialProviderId: String = ""
    private var initialApiKey: String = ""

    override fun getDisplayName(): String = "Semantic Identifiers"

    override fun createComponent(): JComponent {
        val providers = ApiProviderFactory.getAllProviders()
        val providerNames = providers.map { it.providerName }.toTypedArray()
        
        providerComboBox = ComboBox(DefaultComboBoxModel(providerNames))
        apiKeyField = JBPasswordField()

        return panel {
            group("AI Provider Settings") {
                row("Select Provider:") {
                    cell(providerComboBox!!)
                        .align(Align.FILL)
                        .comment("Pick the AI service you want to use for naming suggestions.")
                }
                row("API Key:") {
                    cell(apiKeyField!!)
                        .align(Align.FILL)
                        .comment("Securely stored in your OS credential store.")
                }
            }
        }.also { reset() }
    }

    override fun isModified(): Boolean {
        val selectedIndex = providerComboBox?.selectedIndex ?: -1
        if (selectedIndex == -1) return false

        val currentProvider = ApiProviderFactory.getAllProviders()[selectedIndex].id
        val currentApiKey = String(apiKeyField?.password ?: charArrayOf())

        return currentProvider != initialProviderId || currentApiKey != initialApiKey
    }

    override fun apply() {
        val selectedIndex = providerComboBox?.selectedIndex ?: return
        val provider = ApiProviderFactory.getAllProviders()[selectedIndex]
        val enteredKey = String(apiKeyField?.password ?: charArrayOf())

        // Save selection and key
        ApiKeySettings.selectedProvider = provider.id
        ApiKeySettings.setApiKey(provider.id, enteredKey.ifBlank { null })

        // Update local cache
        initialProviderId = provider.id
        initialApiKey = enteredKey
    }

    override fun reset() {
        // Jump off UI thread for potentially slow secure store calls
        ApplicationManager.getApplication().executeOnPooledThread {
            val selectedId = ApiKeySettings.selectedProvider
            val providers = ApiProviderFactory.getAllProviders()
            val selectedIndex = providers.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
            
            val savedKey = ApiKeySettings.getApiKey(providers[selectedIndex].id) ?: ""

            // Return to UI thread to update components
            ApplicationManager.getApplication().invokeLater {
                providerComboBox?.selectedIndex = selectedIndex
                apiKeyField?.text = savedKey
                
                initialProviderId = selectedId
                initialApiKey = savedKey
            }
        }
    }

    override fun disposeUIResources() {
        providerComboBox = null
        apiKeyField = null
    }
}
