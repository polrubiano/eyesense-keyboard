package pol.rubiano.eyesensekeyboard.keyboard.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import pol.rubiano.eyesensekeyboard.hexgrid.domain.CornerPosition
import pol.rubiano.eyesensekeyboard.hexgrid.domain.HexCoord
import pol.rubiano.eyesensekeyboard.hexgrid.domain.PanelSlot
import pol.rubiano.eyesensekeyboard.keyboard.domain.KeyAction
import pol.rubiano.eyesensekeyboard.keyboard.domain.KeyAssignment

class KeyboardViewModelTest {

    private val viewModel = KeyboardViewModel()

    /** The slot that holds [letter] in the current layout, so tests don't depend on positions. */
    private fun slotOf(letter: String): PanelSlot =
        viewModel.keyboard.panel.slots.first { viewModel.keyboard.keyAt(it) == KeyAssignment.Text(letter) }

    /** Same idea for functions: find the corner by what it does, not by where it is. */
    private fun slotOf(action: KeyAction): PanelSlot =
        viewModel.keyboard.panel.slots.first { viewModel.keyboard.keyAt(it) == KeyAssignment.Function(action) }

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
    fun `the delete corner removes the last letter`() {
        listOf("h", "o", "l", "a").forEach { viewModel.onSlotClick(slotOf(it)) }
        viewModel.onSlotClick(slotOf(KeyAction.DeleteCharacter))
        assertEquals("hol", viewModel.text.value)
    }

    @Test
    fun `the other corners don't change the text yet`() {
        viewModel.onSlotClick(slotOf("e"))
        CornerPosition.entries
            .map { PanelSlot.Corner(it) }
            .filter { it != slotOf(KeyAction.DeleteCharacter) }
            .forEach { viewModel.onSlotClick(it) }
        assertEquals("e", viewModel.text.value)
    }

    @Test
    fun `the centre hexagon holds the most frequent letter`() {
        viewModel.onSlotClick(PanelSlot.Cell(HexCoord.ORIGIN))
        assertEquals("e", viewModel.text.value)
    }
}
