package com.github.nikolaborisov04.semanticidentifiers

import com.github.nikolaborisov04.semanticidentifiers.logic.NameSuggester
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MyPluginTest : BasePlatformTestCase() {

    fun testFindNamedElementReturnsNullForNullInput() {
        assertNull(NameSuggester.findNamedElement(null))
    }

    fun testFindNamedElementFindsVariableInKotlinFile() {
        val psiFile = myFixture.configureByText("test.kt", "val fo<caret>o = 42")
        val element = psiFile.findElementAt(myFixture.caretOffset)
        val namedElement = NameSuggester.findNamedElement(element)
        assertNotNull(namedElement)
        assertEquals("foo", namedElement?.name)
    }

    fun testFindNamedElementFindsMethodInJavaFile() {
        val psiFile = myFixture.configureByText("Test.java",
            "class Test { void my<caret>Method() {} }")
        val element = psiFile.findElementAt(myFixture.caretOffset)
        val namedElement = NameSuggester.findNamedElement(element)
        assertNotNull(namedElement)
        assertEquals("myMethod", namedElement?.name)
    }
}
