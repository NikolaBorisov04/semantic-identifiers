package com.github.nikolaborisov04.semanticidentifiers.actions

import com.github.nikolaborisov04.semanticidentifiers.logic.NameSuggester
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class SuggestNameAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return

        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset)

        // Delegate to shared logic
        NameSuggester.suggestAndShow(project, element)
    }
}