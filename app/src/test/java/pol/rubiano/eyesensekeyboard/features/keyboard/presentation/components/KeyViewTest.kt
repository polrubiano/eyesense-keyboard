package pol.rubiano.eyesensekeyboard.features.keyboard.presentation.components

import org.junit.Assert.assertEquals
import org.junit.Test
import pol.rubiano.eyesensekeyboard.R
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAction
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAssignment

class KeyViewTest {

    @Test
    fun `a letter shows in uppercase with the character style`() {
        assertEquals(
            KeyFace(KeyFace.Content.Glyph("Ñ"), KeyFace.Style.CHARACTER),
            KeyAssignment.Text("ñ").face(),
        )
    }

    @Test
    fun `the space shows its icon with the action style`() {
        assertEquals(
            KeyFace(
                KeyFace.Content.Icon(R.drawable.ic_space_bar, R.string.key_space),
                KeyFace.Style.ACTION
            ),
            KeyAssignment.Text(" ").face(),
        )
    }

    @Test
    fun `a phrase that contains spaces is still text`() {
        assertEquals(KeyFace.Style.CHARACTER, KeyAssignment.Text("¿qué tal?").face().style)
    }

    @Test
    fun `every function shows its own icon with the action style`() {
        KeyAction.all.forEach { action ->
            assertEquals(
                KeyFace(
                    KeyFace.Content.Icon(action.icon(), action.description()),
                    KeyFace.Style.ACTION
                ),
                KeyAssignment.Function(action).face(),
            )
        }
    }
}
