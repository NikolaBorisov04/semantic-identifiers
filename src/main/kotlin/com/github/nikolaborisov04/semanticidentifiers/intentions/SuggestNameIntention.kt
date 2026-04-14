package com.github.nikolaborisov04.semanticidentifiers.intentions

import com.github.nikolaborisov04.semanticidentifiers.logic.NameSuggester
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class SuggestNameIntention : IntentionAction
{
    override fun getText(): String = "Suggest Semantic Name"
    override fun getFamilyName(): String = "Semantic Identifiers"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean
    {
        if (editor == null || file == null) return false
        val element = file.findElementAt(editor.caretModel.offset)
        return NameSuggester.findNamedElement(element) != null
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?)
    {
        if (editor == null || file == null) return
        val element = file.findElementAt(editor.caretModel.offset)
        NameSuggester.suggestAndShow(project, element)
    }

    override fun startInWriteAction(): Boolean = false
}