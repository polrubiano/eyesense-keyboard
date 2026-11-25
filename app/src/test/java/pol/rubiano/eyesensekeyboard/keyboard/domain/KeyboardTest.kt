package pol.rubiano.eyesensekeyboard.keyboard.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pol.rubiano.eyesensekeyboard.hexgrid.domain.CornerPosition
import pol.rubiano.eyesensekeyboard.hexgrid.domain.HexCoord
import pol.rubiano.eyesensekeyboard.hexgrid.domain.PanelSlot

class KeyboardTest {

    private val keyboard = Keyboard(Presets.PANEL, Presets.SPANISH, Presets.DEFAULT_FUNCTIONS)

    private val mySpanish = Presets.SPANISH.copyAsUser(LayoutId("user.es"), "Castellano (mío)")

    // ---- Resolving slots ----

    @Test
    fun `a hexagon resolves to the character layout`() {
        assertEquals(KeyAssignment.Text("e"), keyboard.keyAt(PanelSlot.Cell(HexCoord.ORIGIN)))
    }

    @Test
    fun `a corner key resolves to the function layout`() {
        assertEquals(
            KeyAssignment.Function(KeyAction.DeleteCharacter),
            keyboard.keyAt(PanelSlot.Corner(CornerPosition.TOP_RIGHT)),
        )
    }

    @Test
    fun `an emptied hexagon has nothing`() {
        val withGap = keyboard.withCharacters(mySpanish.clear(HexCoord.ORIGIN))
        assertNull(withGap.keyAt(PanelSlot.Cell(HexCoord.ORIGIN)))
    }

    @Test
    fun `changing language keeps the functions`() {
        val english = keyboard.withCharacters(Presets.ENGLISH)
        CornerPosition.entries.forEach { position ->
            val slot = PanelSlot.Corner(position)
            assertEquals(keyboard.keyAt(slot), english.keyAt(slot))
        }
        assertEquals(KeyAssignment.Text("t"), english.keyAt(PanelSlot.Cell(Presets.PANEL.cells[1])))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a layout with keys outside the panel is rejected`() {
        val outside = mySpanish.assign(HexCoord(10, 0), KeyAssignment.Text("x"))
        Keyboard(Presets.PANEL, outside, Presets.DEFAULT_FUNCTIONS)
    }

    // ---- Editing ----

    @Test
    fun `the user can put a function in a hexagon`() {
        val edited = mySpanish.assign(HexCoord.ORIGIN, KeyAssignment.Function(KeyAction.Speak))
        assertEquals(
            KeyAssignment.Function(KeyAction.Speak),
            keyboard.withCharacters(edited).keyAt(PanelSlot.Cell(HexCoord.ORIGIN)),
        )
    }

    // ---- Merged keys (space bar) ----

    private val spanishBar = setOf(HexCoord(0, 2), HexCoord(1, 2))

    @Test
    fun `the space bar is a single slot that writes a space`() {
        val bar = PanelSlot.Merged(spanishBar)
        assertEquals(1, keyboard.panel.slots.count { it == bar })
        assertEquals(KeyAssignment.Text(" "), keyboard.keyAt(bar))
    }

    @Test
    fun `changing language changes the space bar`() {
        val english = keyboard.withCharacters(Presets.ENGLISH)
        assertTrue(PanelSlot.Merged(setOf(HexCoord(-1, 2), HexCoord(0, 2), HexCoord(1, 2))) in english.panel.slots)
        assertFalse(PanelSlot.Merged(spanishBar) in english.panel.slots)
    }

    @Test
    fun `neighbours with the same content are not merged unless listed`() {
        val twoA = mySpanish.copy(merged = emptyList())
            .assign(HexCoord.ORIGIN, KeyAssignment.Text("a"))
            .assign(HexCoord(1, 0), KeyAssignment.Text("a"))
        val panel = keyboard.withCharacters(twoA).panel
        assertTrue(PanelSlot.Cell(HexCoord.ORIGIN) in panel.slots)
        assertTrue(PanelSlot.Cell(HexCoord(1, 0)) in panel.slots)
    }
}
