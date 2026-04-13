package com.github.nikolaborisov04.semanticidentifiers.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil

class SuggestNameAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return

        //Take an element at the cursor position
        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset)

        //Search for the nearest element that has a name (variable, function, class)
        val namedElement = PsiTreeUtil.getParentOfType(element, PsiNamedElement::class.java)

        if (namedElement != null) {
            val currentName = namedElement.name
            Messages.showInfoMessage(project, "Current name: $currentName. \nAI will soon suggest a better one!", "Semantic Identifiers")
        } else {
            Messages.showErrorDialog(project, "Please place the cursor on a variable or function name.", "No Identifier Found")
        }
    }
}