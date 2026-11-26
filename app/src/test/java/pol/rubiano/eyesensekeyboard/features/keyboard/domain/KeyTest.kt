package pol.rubiano.eyesensekeyboard.features.keyboard.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KeyTest {

    @Test
    fun `function ids are unique`() {
        val ids = KeyAction.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `function ids are stable - they must never change`() {
        // If this test fails, saved layouts would stop loading.
        assertEquals("delete_character", KeyAction.DeleteCharacter.id)
        assertEquals("clear_text", KeyAction.ClearText.id)
        assertEquals("speak", KeyAction.Speak.id)
        assertEquals("select_emotion", KeyAction.SelectEmotion.id)
    }

    @Test
    fun `every function can be recovered from its id`() {
        KeyAction.all.forEach { assertEquals(it, KeyAction.fromId(it.id)) }
        assertNull(KeyAction.fromId("does_not_exist"))
    }

    @Test
    fun `the catalogue includes every function`() {
        // Exhaustive when: if someone adds a function, this stops compiling
        // until they look at it here, and the size check reminds them to add it to `all`.
        KeyAction.all.forEach { action ->
            when (action) {
                KeyAction.DeleteCharacter, KeyAction.ClearText, KeyAction.Speak, KeyAction.SelectEmotion -> Unit
            }
        }
        assertEquals(4, KeyAction.all.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a text key can't be empty`() {
        KeyAssignment.Text("")
    }

    @Test
    fun `a text key accepts a letter, the space or a whole phrase`() {
        listOf("e", " ", "¿qué tal?").forEach { assertEquals(it, KeyAssignment.Text(it).value) }
    }
}
