package pol.rubiano.eyesensekeyboard.features.keyboard.presentation.viewmodels

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pol.rubiano.eyesensekeyboard.app.MainDispatcherRule
import pol.rubiano.eyesensekeyboard.features.keyboard.data.datarepositories.LayoutDataRepository
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datasources.LayoutDbLocalDataSource
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datastore.ActiveLayoutDataStoreManager
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.db.KeyboardDatabase
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexCoord
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.PanelSlot
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Emotion
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAction
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAssignment
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Keyboard
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Writing
import java.io.File

/**
 * Only the wiring: taps reach
 * [Writing.pressed][pol.rubiano.eyesensekeyboard.features.keyboard.domain.pressed] through the keyboard on screen.
 * What each key does is tested in `WritingTest`.
 */
@RunWith(RobolectricTestRunner::class)
class KeyboardViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @get:Rule
    val folder = TemporaryFolder()

    // Built by hand, without Hilt: the real repository on real storage, Room in memory and
    // DataStore in a temporary file. Each test starts with both empty.
    private val db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        KeyboardDatabase::class.java,
    ).allowMainThreadQueries().build()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Lazy: the temporary folder only exists once the test is running.
    private val repository by lazy {
        LayoutDataRepository(
            LayoutDbLocalDataSource(db.layoutDao()),
            ActiveLayoutDataStoreManager(
                PreferenceDataStoreFactory.create(scope = scope, produceFile = { File(folder.root, "settings.preferences_pb") }),
            ),
        )
    }

    // Lazy: the ViewModel launches work on the main dispatcher, which the rule only swaps in
    // once the test is running, not when the class is built.
    private val viewModel by lazy { KeyboardViewModel(repository) }

    /**
     * The ViewModel stops listening before the storage closes, so it never reads a closed database.
     *
     * `cancel()` only asks to stop: Room may be delivering a value from its own thread at that
     * very moment, and that delivery touches `Dispatchers.Main` while the rule resets it
     * ("Dispatchers.Main is used concurrently with setting it"). `join()` waits until the
     * ViewModel's work has really finished.
     */
    @After
    fun tearDown() = runBlocking {
        viewModel.viewModelScope.coroutineContext.job.cancelAndJoin()
        scope.cancel()
        db.close()
    }

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
    fun `the clear button empties the text and keeps the emotion`() {
        tap(KeyAssignment.Function(KeyAction.SelectEmotion))
        tap(KeyAssignment.Choice(Emotion.HAPPY))
        listOf("h", "o", "l", "a").forEach { tap(KeyAssignment.Text(it)) }
        viewModel.onClearText()
        assertEquals("", state.text)
        assertEquals(Emotion.HAPPY, state.emotion)
    }

    @Test
    fun `the settings key opens the settings and the close hexagon closes them`() {
        tap(KeyAssignment.Function(KeyAction.OpenSettings))
        assertEquals(true, state.settingsOpen)
        viewModel.onCloseSettings()
        assertEquals(false, state.settingsOpen)
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

    /**
     * Real storage answers from its own threads, a moment after the write. So instead of asserting
     * right away, wait until the keyboard east of the centre holds [expected], or fail after 5 s.
     *
     * The tests use `runBlocking`, not `runTest`: `runTest`'s virtual time would make the
     * timeout fire at once, before Room or DataStore answer.
     */
    private suspend fun awaitEastOfCentre(expected: KeyAssignment) {
        withTimeout(5_000) {
            viewModel.state.first { it.keyboard.keyAt(PanelSlot.Cell(HexCoord(1, 0))) == expected }
        }
    }

    @Test
    fun `it starts with the layouts that are active`() = runBlocking {
        repository.activateCharacterLayout(Presets.ENGLISH.id)
        awaitEastOfCentre(KeyAssignment.Text("t"))
        assertEquals(KeyAssignment.Text("t"), eastOfCentre)
    }

    @Test
    fun `changing the active language changes the keyboard and keeps what's written`() = runBlocking {
        listOf("h", "o").forEach { tap(KeyAssignment.Text(it)) }
        repository.activateCharacterLayout(Presets.ENGLISH.id)
        awaitEastOfCentre(KeyAssignment.Text("t"))
        assertEquals("ho", state.text)
    }

    @Test
    fun `an edit to the active layout reaches the keyboard`() = runBlocking {
        val mine = Presets.SPANISH.copyAsUser(LayoutId("user.es"), "Castellano 2")
        repository.save(mine)
        repository.activateCharacterLayout(mine.id)
        repository.save(mine.assign(HexCoord(1, 0), KeyAssignment.Text("ñ")))
        awaitEastOfCentre(KeyAssignment.Text("ñ"))
        assertEquals(KeyAssignment.Text("ñ"), eastOfCentre)
    }
}
