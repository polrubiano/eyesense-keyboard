package pol.rubiano.eyesensekeyboard.keyboard.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import pol.rubiano.eyesensekeyboard.hexgrid.domain.CornerPosition
import pol.rubiano.eyesensekeyboard.hexgrid.domain.HexCoord
import pol.rubiano.eyesensekeyboard.hexgrid.domain.PanelSlot
import pol.rubiano.eyesensekeyboard.keyboard.domain.KeyAssignment

class KeyboardViewModelTest {

    private val viewModel = KeyboardViewModel()

    /** The slot that holds [letter] in the current layout, so tests don't depend on positions. */
    private fun slotOf(letter: String): PanelSlot =
        viewModel.keyboard.panel.slots.first { viewModel.keyboard.keyAt(it) == KeyAssignment.Text(letter) }

    @Test
    fun `the text starts empty`() {
        assertEquals("", viewModel.text.value)
    }

    @Test
    fun `tapping hexagons writes the word`() {
        listOf("h", "o", "l", "a").forEach { viewModel.onSlotClick(slotOf(it)) }
        assertEquals("hola", viewModel.text.value)
    }

    @Test
    fun `the space bar separates words`() {
        listOf("s", "i", " ", "n", "o").forEach { viewModel.onSlotClick(slotOf(it)) }
        assertEquals("si no", viewModel.text.value)
    }

    @Test
    fun `the corners don't change the text yet`() {
        viewModel.onSlotClick(slotOf("e"))
        CornerPosition.entries.forEach { viewModel.onSlotClick(PanelSlot.Corner(it)) }
        assertEquals("e", viewModel.text.value)
    }

    @Test
    fun `the centre hexagon holds the most frequent letter`() {
        viewModel.onSlotClick(PanelSlot.Cell(HexCoord.ORIGIN))
        assertEquals("e", viewModel.text.value)
    }
}
