package pol.rubiano.eyesensekeyboard.features.keyboard.presentation.viewmodels

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import pol.rubiano.eyesensekeyboard.app.MainDispatcherRule
import pol.rubiano.eyesensekeyboard.features.keyboard.data.datarepositories.LayoutDataRepository
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datasources.ActiveLayoutMemLocalDataSource
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datasources.LayoutMemLocalDataSource
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexCoord
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.PanelSlot
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Emotion
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAction
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAssignment
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Keyboard
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Writing

/**
 * Only the wiring: taps reach
 * [Writing.pressed][pol.rubiano.eyesensekeyboard.features.keyboard.domain.pressed] through the keyboard on screen.
 * What each key does is tested in `WritingTest`.
 */
class KeyboardViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    // Built by hand, without Hilt: the rules of the real repository, with layouts kept in memory.
    private val repository = LayoutDataRepository(LayoutMemLocalDataSource(), ActiveLayoutMemLocalDataSource())

    // Lazy: the ViewModel launches work on the main dispatcher, which the rule only swaps in
    // once the test is running, not when the class is built.
    private val viewModel by lazy { KeyboardViewModel(repository) }

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

    // ---- the active layout ----

    /** East of the centre: "a" in Spanish, "t" in English. */
    private val eastOfCentre get() = state.keyboard.keyAt(PanelSlot.Cell(HexCoord(1, 0)))

    @Test
    fun `it starts with the layouts that are active`() = runTest {
        repository.activateCharacterLayout(Presets.ENGLISH.id)
        assertEquals(KeyAssignment.Text("t"), eastOfCentre)
    }

    @Test
    fun `changing the active language changes the keyboard and keeps what's written`() = runTest {
        listOf("h", "o").forEach { tap(KeyAssignment.Text(it)) }
        repository.activateCharacterLayout(Presets.ENGLISH.id)
        assertEquals(KeyAssignment.Text("t"), eastOfCentre)
        assertEquals("ho", state.text)
    }

    @Test
    fun `an edit to the active layout reaches the keyboard`() = runTest {
        val mine = Presets.SPANISH.copyAsUser(LayoutId("user.es"), "Castellano 2")
        repository.save(mine)
        repository.activateCharacterLayout(mine.id)
        repository.save(mine.assign(HexCoord(1, 0), KeyAssignment.Text("ñ")))
        assertEquals(KeyAssignment.Text("ñ"), eastOfCentre)
    }
}
