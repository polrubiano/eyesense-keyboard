package pol.rubiano.eyesensekeyboard.features.keyboard.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.CornerPosition
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexCoord
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexPanel

class LayoutsTest {

    private val panel = Presets.PANEL
    private val space = KeyAssignment.Text(" ")

    private val mySpanish = Presets.SPANISH.copyAsUser(LayoutId("user.es"), "Castellano (mío)")

    // ---- Generator ----

    @Test
    fun `the generator puts the first one at the centre and the following ones by closeness`() {
        val layout =
            characterLayoutByFrequency(LayoutId("test"), "Test", panel, listOf("x", "y", "z"))
        assertEquals(KeyAssignment.Text("x"), layout.cells[HexCoord.ORIGIN])
        assertEquals(KeyAssignment.Text("y"), layout.cells[panel.cells[1]])
        assertEquals(KeyAssignment.Text("z"), layout.cells[panel.cells[2]])
        assertEquals(3, layout.cells.size)
    }

    @Test
    fun `the letters skip the pinned cells and reorder themselves`() {
        val pinnedCell = panel.cells[1]
        val layout = characterLayoutByFrequency(
            LayoutId("test"), "Test", panel,
            ranked = listOf("x", "y"),
            pinned = mapOf(pinnedCell to space),
        )
        assertEquals(space, layout.cells[pinnedCell])
        assertEquals(KeyAssignment.Text("x"), layout.cells[panel.cells[0]])
        assertEquals(KeyAssignment.Text("y"), layout.cells[panel.cells[2]]) // jumped over position 1
    }

    @Test(expected = IllegalArgumentException::class)
    fun `the generator fails if there are more keys than hexagons`() {
        val singleHexagon = HexPanel(rowsAroundCenter = 0, rings = 0)
        characterLayoutByFrequency(LayoutId("test"), "Test", singleHexagon, listOf("a", "b"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `the pinned keys also count towards capacity`() {
        val singleHexagon = HexPanel(rowsAroundCenter = 0, rings = 0)
        characterLayoutByFrequency(
            LayoutId("test"), "Test", singleHexagon,
            ranked = listOf("a"),
            pinned = mapOf(HexCoord.ORIGIN to space),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a pinned key outside the panel is rejected`() {
        characterLayoutByFrequency(
            LayoutId("test"), "Test", panel,
            ranked = emptyList(),
            pinned = mapOf(HexCoord(10, 0) to space),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `the generator fails if there are repeated letters`() {
        characterLayoutByFrequency(LayoutId("test"), "Test", panel, listOf("a", "a"))
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

    // ---- Merged keys (space bar) ----

    private val spanishBar = setOf(HexCoord(0, 2), HexCoord(1, 2))

    @Test(expected = IllegalArgumentException::class)
    fun `a merged key with different contents is rejected`() {
        mySpanish.copy(merged = listOf(setOf(HexCoord.ORIGIN, HexCoord(1, 0))))
    }

    @Test
    fun `editing one hexagon of a merged key edits the whole key`() {
        val edited = mySpanish.assign(HexCoord(0, 2), KeyAssignment.Text("."))
        spanishBar.forEach { assertEquals(KeyAssignment.Text("."), edited.cells[it]) }
    }

    @Test
    fun `swapping a letter with the space bar exchanges their contents, not their shapes`() {
        val swapped = mySpanish.swap(HexCoord.ORIGIN, HexCoord(1, 2))
        assertEquals(KeyAssignment.Text(" "), swapped.cells[HexCoord.ORIGIN])
        spanishBar.forEach { assertEquals(KeyAssignment.Text("e"), swapped.cells[it]) }
        assertEquals(mySpanish.merged, swapped.merged)
    }

    @Test
    fun `swapping two hexagons of the same merged key changes nothing`() {
        assertEquals(mySpanish, mySpanish.swap(HexCoord(0, 2), HexCoord(1, 2)))
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
