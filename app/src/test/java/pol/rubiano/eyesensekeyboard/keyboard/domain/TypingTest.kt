package pol.rubiano.eyesensekeyboard.keyboard.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TypingTest {

    @Test
    fun `a letter goes at the end`() {
        assertEquals("hol", "ho".typed(KeyAssignment.Text("l")))
    }

    @Test
    fun `typing on an empty text starts it`() {
        assertEquals("e", "".typed(KeyAssignment.Text("e")))
    }

    @Test
    fun `the space and whole phrases are typed the same way`() {
        assertEquals("hola ", "hola".typed(KeyAssignment.Text(" ")))
        assertEquals("hola ¿qué tal?", "hola ".typed(KeyAssignment.Text("¿qué tal?")))
    }

    @Test
    fun `functions don't change the text yet`() {
        KeyAction.all.forEach { action ->
            assertEquals("hola", "hola".typed(KeyAssignment.Function(action)))
        }
    }

    @Test
    fun `a free slot doesn't change the text`() {
        assertEquals("hola", "hola".typed(null))
    }

    @Test
    fun `a sequence of keys builds the word`() {
        val keys = listOf("h", "o", "l", "a").map { KeyAssignment.Text(it) }
        assertEquals("hola", keys.fold("") { text, key -> text.typed(key) })
    }
}
