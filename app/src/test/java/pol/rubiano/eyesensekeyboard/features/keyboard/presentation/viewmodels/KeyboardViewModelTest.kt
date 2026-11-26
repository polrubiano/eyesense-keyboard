package pol.rubiano.eyesensekeyboard.features.keyboard.presentation.viewmodels

import org.junit.Assert.assertEquals
import org.junit.Test
import pol.rubiano.eyesensekeyboard.features.keyboard.data.datarepositories.LayoutDataRepository
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datasources.LayoutMemLocalDataSource
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexCoord
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.PanelSlot
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Emotion
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAction
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAssignment
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Keyboard
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Writing

/**
 * Only the wiring: taps reach
 * [Writing.pressed][pol.rubiano.eyesensekeyboard.features.keyboard.domain.pressed] through the keyboard on screen.
 * What each key does is tested in `WritingTest`.
 */
class KeyboardViewModelTest {

    // Built by hand, without Hilt: the rules of the real repository, with layouts kept in memory.
    private val viewModel = KeyboardViewModel(LayoutDataRepository(LayoutMemLocalDataSource()))

    private val state get() = viewModel.state.value

    /** The slot that holds [key] on the keyboard on screen, so tests don't depend on positions. */
    private fun slotOf(key: KeyAssignment): PanelSlot =
        state.keyboard.panel.slots.first { state.keyboard.keyAt(it) == key }

    private fun tap(key: KeyAssignment) = viewModel.onSlotClick(slotOf(key))

    @Test
    fun `it starts with an empty state on the spanish keyboard`() {
        assertEquals(Writing(Keyboard(Presets.PANEL, Presets.SPANISH, Presets.DEFAULT_FUNCTIONS)), state)
    }

    @Test
    fun `tapping hexagons writes the word`() {
        listOf("h", "o", "l", "a").forEach { tap(KeyAssignment.Text(it)) }
        assertEquals("hola", state.text)
    }

    @Test
    fun `tapping a corner reaches its function`() {
        listOf("h", "o", "l", "a").forEach { tap(KeyAssignment.Text(it)) }
        tap(KeyAssignment.Function(KeyAction.DeleteCharacter))
        assertEquals("hol", state.text)
    }

    @Test
    fun `taps are resolved on the open menu, not on the normal keyboard`() {
        tap(KeyAssignment.Function(KeyAction.SelectEmotion))
        tap(KeyAssignment.Choice(Emotion.SAD))
        assertEquals(Emotion.SAD, state.emotion)
    }

    @Test
    fun `tapping speak shows the message with its emotion and empties the field`() {
        tap(KeyAssignment.Function(KeyAction.SelectEmotion))
        tap(KeyAssignment.Choice(Emotion.HAPPY))
        listOf("h", "o", "l", "a").forEach { tap(KeyAssignment.Text(it)) }
        tap(KeyAssignment.Function(KeyAction.Speak))
        assertEquals(listOf("[happy][hola]"), state.spoken.map { it.payload })
        assertEquals("", state.text)
    }

    @Test
    fun `the centre hexagon holds the most frequent letter`() {
        viewModel.onSlotClick(PanelSlot.Cell(HexCoord.ORIGIN))
        assertEquals("e", state.text)
    }
}
