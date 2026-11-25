package pol.rubiano.eyesensekeyboard.keyboard.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pol.rubiano.eyesensekeyboard.hexgrid.domain.CornerPosition
import pol.rubiano.eyesensekeyboard.hexgrid.domain.HexCoord

class PresetsTest {

    private val panel = Presets.PANEL
    private val space = KeyAssignment.Text(" ")

    /** The layout row by row (top to bottom, left to right), as we draw it in the sketches. */
    private fun rows(layout: CharacterLayout): List<String> =
        (-2..2).map { r ->
            layout.cells.entries
                .filter { it.key.r == r }
                .sortedBy { it.key.q }
                .joinToString("") { (it.value as KeyAssignment.Text).value.uppercase().replace(" ", "␣") }
        }

    private fun letters(layout: CharacterLayout) =
        layout.cells.values.map { (it as KeyAssignment.Text).value }.filter { it != " " }

    // ---- Language presets ----

    @Test
    fun `spanish - matches the agreed sketch`() {
        assertEquals(
            listOf(
                "XBGVK",
                "ÑPNIYW",
                "JMREADQ",
                "ZUSOLH",
                "FTC␣␣",
            ),
            rows(Presets.SPANISH),
        )
    }

    @Test
    fun `english - matches the agreed sketch`() {
        assertEquals(
            listOf(
                "XMWFQ",
                "JUNSGZ",
                "KCIETHY",
                "VLOARP",
                "BD␣␣␣",
            ),
            rows(Presets.ENGLISH),
        )
    }

    @Test
    fun `both languages fill the panel - no free hexagons`() {
        Presets.characterLayouts.forEach { layout ->
            assertEquals(panel.cells.toSet(), layout.cells.keys)
        }
    }

    @Test
    fun `spanish - 27 letters with no repeats and a 2-hexagon space bar`() {
        val letters = letters(Presets.SPANISH)
        assertEquals(27, letters.size)
        assertEquals(letters.size, letters.toSet().size)
        assertEquals(2, Presets.SPANISH.cells.values.count { it == space })
    }

    @Test
    fun `english - 26 letters with no repeats and a 3-hexagon space bar`() {
        val letters = letters(Presets.ENGLISH)
        assertEquals(26, letters.size)
        assertEquals(letters.size, letters.toSet().size)
        assertEquals(3, Presets.ENGLISH.cells.values.count { it == space })
    }

    @Test
    fun `the space bar is at the right-hand end of the bottom row`() {
        Presets.characterLayouts.forEach { layout ->
            val bottomRow = layout.cells.entries.filter { it.key.r == 2 }.sortedBy { it.key.q }
            val spaces = bottomRow.takeLastWhile { it.value == space }
            // Every space is at the end: there are no letters to the right of the space.
            assertEquals(layout.cells.values.count { it == space }, spaces.size)
        }
    }

    @Test
    fun `the spanish space hexagons are also space in english - the bar grows, it doesn't move`() {
        val spanishSpaces = Presets.SPANISH.cells.filterValues { it == space }.keys
        val englishSpaces = Presets.ENGLISH.cells.filterValues { it == space }.keys
        assertTrue(englishSpaces.containsAll(spanishSpaces))
    }

    @Test
    fun `the e is at the centre and the second most frequent letter in the first ring`() {
        assertEquals(KeyAssignment.Text("e"), Presets.SPANISH.cells[HexCoord.ORIGIN])
        assertEquals(KeyAssignment.Text("a"), Presets.SPANISH.cells[panel.cells[1]])
        assertEquals(KeyAssignment.Text("e"), Presets.ENGLISH.cells[HexCoord.ORIGIN])
        assertEquals(KeyAssignment.Text("t"), Presets.ENGLISH.cells[panel.cells[1]])
    }

    // ---- Functions ----

    @Test
    fun `default functions - one per corner, in the agreed position`() {
        assertEquals(
            mapOf(
                CornerPosition.TOP_LEFT to KeyAction.ClearText,
                CornerPosition.TOP_RIGHT to KeyAction.DeleteCharacter,
                CornerPosition.BOTTOM_LEFT to KeyAction.SelectEmotion,
                CornerPosition.BOTTOM_RIGHT to KeyAction.Speak,
            ),
            Presets.DEFAULT_FUNCTIONS.corners,
        )
    }

    @Test
    fun `presets have unique ids and are of PRESET origin`() {
        val all = Presets.characterLayouts.map { it.id to it.origin } +
            Presets.functionLayouts.map { it.id to it.origin }
        assertEquals(all.size, all.map { it.first }.toSet().size)
        assertTrue(all.all { it.second == LayoutOrigin.PRESET })
    }
}
