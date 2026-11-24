package pol.rubiano.eyesensekeyboard.keyboard.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pol.rubiano.eyesensekeyboard.hexgrid.domain.HexCoord
import pol.rubiano.eyesensekeyboard.hexgrid.domain.PanelSlot
import pol.rubiano.eyesensekeyboard.keyboard.domain.Emotion
import pol.rubiano.eyesensekeyboard.keyboard.domain.KeyAction
import pol.rubiano.eyesensekeyboard.keyboard.domain.KeyAssignment
import pol.rubiano.eyesensekeyboard.keyboard.domain.Option

class KeyboardViewModelTest {

    private val viewModel = KeyboardViewModel()

    /** The keyboard on screen right now: the normal one, or an open menu. */
    private val keyboard get() = viewModel.keyboard.value

    /** The slot that holds the option [option] in the open menu (null = "none"). */
    private fun slotOf(option: Option?): PanelSlot =
        keyboard.panel.slots.first { keyboard.keyAt(it) == KeyAssignment.Choice(option) }

    /** The slot that holds [letter] in the current layout, so tests don't depend on positions. */
    private fun slotOf(letter: String): PanelSlot =
        keyboard.panel.slots.first { keyboard.keyAt(it) == KeyAssignment.Text(letter) }

    /** Same idea for functions: find the corner by what it does, not by where it is. */
    private fun slotOf(action: KeyAction): PanelSlot =
        keyboard.panel.slots.first { keyboard.keyAt(it) == KeyAssignment.Function(action) }

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
    fun `the clear corner wipes the whole text`() {
        listOf("s", "i", " ", "n", "o").forEach { viewModel.onSlotClick(slotOf(it)) }
        viewModel.onSlotClick(slotOf(KeyAction.ClearText))
        assertEquals("", viewModel.text.value)
    }

    @Test
    fun `the speak corner doesn't change the text yet`() {
        viewModel.onSlotClick(slotOf("e"))
        viewModel.onSlotClick(slotOf(KeyAction.Speak))
        assertEquals("e", viewModel.text.value)
    }

    // ---- Emotion menu ----

    @Test
    fun `there's no emotion at first`() {
        assertNull(viewModel.emotion.value)
    }

    @Test
    fun `the emotion corner opens its menu`() {
        viewModel.onSlotClick(slotOf(KeyAction.SelectEmotion))
        assertEquals(KeyAssignment.Choice(null), keyboard.keyAt(PanelSlot.Cell(HexCoord.ORIGIN)))
    }

    @Test
    fun `picking an emotion stores it and closes the menu`() {
        viewModel.onSlotClick(slotOf(KeyAction.SelectEmotion))
        viewModel.onSlotClick(slotOf(Emotion.SAD))
        assertEquals(Emotion.SAD, viewModel.emotion.value)
        assertEquals(KeyAssignment.Text("e"), keyboard.keyAt(PanelSlot.Cell(HexCoord.ORIGIN)))
    }

    @Test
    fun `the centre removes the emotion and closes the menu`() {
        viewModel.onSlotClick(slotOf(KeyAction.SelectEmotion))
        viewModel.onSlotClick(slotOf(Emotion.HAPPY))
        viewModel.onSlotClick(slotOf(KeyAction.SelectEmotion))
        viewModel.onSlotClick(slotOf(null))
        assertNull(viewModel.emotion.value)
        assertEquals(KeyAssignment.Text("e"), keyboard.keyAt(PanelSlot.Cell(HexCoord.ORIGIN)))
    }

    @Test
    fun `the emotion corner closes the menu without changes`() {
        viewModel.onSlotClick(slotOf(KeyAction.SelectEmotion))
        viewModel.onSlotClick(slotOf(Emotion.ANGRY))
        viewModel.onSlotClick(slotOf(KeyAction.SelectEmotion))
        viewModel.onSlotClick(slotOf(KeyAction.SelectEmotion))
        assertEquals(Emotion.ANGRY, viewModel.emotion.value)
        assertEquals(KeyAssignment.Text("e"), keyboard.keyAt(PanelSlot.Cell(HexCoord.ORIGIN)))
    }

    @Test
    fun `the menu doesn't touch the text`() {
        listOf("h", "o", "l", "a").forEach { viewModel.onSlotClick(slotOf(it)) }
        viewModel.onSlotClick(slotOf(KeyAction.SelectEmotion))
        viewModel.onSlotClick(slotOf(Emotion.HAPPY))
        assertEquals("hola", viewModel.text.value)
    }

    @Test
    fun `the free hexagons of the menu do nothing`() {
        viewModel.onSlotClick(slotOf(KeyAction.SelectEmotion))
        val free = keyboard.panel.slots.first { keyboard.keyAt(it) == null }
        viewModel.onSlotClick(free)
        assertEquals(KeyAssignment.Choice(null), keyboard.keyAt(PanelSlot.Cell(HexCoord.ORIGIN)))
        assertEquals("", viewModel.text.value)
    }

    @Test
    fun `the centre hexagon holds the most frequent letter`() {
        viewModel.onSlotClick(PanelSlot.Cell(HexCoord.ORIGIN))
        assertEquals("e", viewModel.text.value)
    }
}
