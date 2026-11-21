package pol.rubiano.eyesensekeyboard.keyboard.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun `a triangle resolves to the function layout`() {
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

    @Test(expected = IllegalStateException::class)
    fun `a preset can't be edited directly`() {
        Presets.SPANISH.assign(HexCoord.ORIGIN, KeyAssignment.Text("a"))
    }

    @Test
    fun `editing a copy leaves the preset untouched`() {
        val edited = mySpanish.assign(HexCoord.ORIGIN, KeyAssignment.Text("a"))
        assertEquals(KeyAssignment.Text("a"), edited.cells[HexCoord.ORIGIN])
        assertEquals(KeyAssignment.Text("e"), Presets.SPANISH.cells[HexCoord.ORIGIN])
        assertEquals(LayoutOrigin.USER, edited.origin)
    }

    @Test
    fun `the user can put a function in a hexagon`() {
        val edited = mySpanish.assign(HexCoord.ORIGIN, KeyAssignment.Function(KeyAction.Speak))
        assertEquals(
            KeyAssignment.Function(KeyAction.Speak),
            keyboard.withCharacters(edited).keyAt(PanelSlot.Cell(HexCoord.ORIGIN)),
        )
    }

    @Test
    fun `the user can put a whole phrase on a key`() {
        val coord = Presets.PANEL.cells.last()
        val edited = mySpanish.assign(coord, KeyAssignment.Text("¿qué tal?"))
        assertEquals(KeyAssignment.Text("¿qué tal?"), edited.cells[coord])
    }

    @Test
    fun `clear empties a key`() {
        val edited = mySpanish.clear(HexCoord.ORIGIN)
        assertNull(edited.cells[HexCoord.ORIGIN])
    }

    @Test
    fun `swap exchanges two keys, and if one is empty the other moves`() {
        val a = HexCoord.ORIGIN
        val b = Presets.PANEL.cells[2]
        val swapped = mySpanish.swap(a, b)
        assertEquals(mySpanish.cells[b], swapped.cells[a])
        assertEquals(mySpanish.cells[a], swapped.cells[b])

        val empty = Presets.PANEL.cells.last()
        val moved = mySpanish.clear(empty).swap(a, empty)
        assertNull(moved.cells[a])
        assertEquals(KeyAssignment.Text("e"), moved.cells[empty])
    }

    @Test
    fun `the function layout can be edited the same way, once copied`() {
        val myFunctions = Presets.DEFAULT_FUNCTIONS
            .copyAsUser(LayoutId("user.functions"), "Mis funciones")
            .swap(CornerPosition.TOP_LEFT, CornerPosition.TOP_RIGHT)
        assertEquals(KeyAction.DeleteCharacter, myFunctions.corners[CornerPosition.TOP_LEFT])
        assertEquals(KeyAction.ClearText, myFunctions.corners[CornerPosition.TOP_RIGHT])
    }
}
