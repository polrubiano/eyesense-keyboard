package pol.rubiano.eyesensekeyboard.features.keyboardsettings.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pol.rubiano.eyesensekeyboard.app.KeyboardStorageRule
import pol.rubiano.eyesensekeyboard.app.MainDispatcherRule
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets

/** The Idioma tab against the real storage: layouts in Room and settings in DataStore. */
@RunWith(RobolectricTestRunner::class)
class KeyboardSettingsViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @get:Rule
    val storage = KeyboardStorageRule()

    private val viewModel by lazy {
        KeyboardSettingsViewModel(storage.layouts, storage.getActiveLayouts, storage.activateCharacterLayout)
    }

    /** Same as in KeyboardViewModelTest: the ViewModel really stops before the storage closes. */
    @After
    fun tearDown() = runBlocking {
        viewModel.viewModelScope.coroutineContext.job.cancelAndJoin()
    }

    /**
     * Storage answers from its own threads, so wait (up to 5 s) for the rows to match.
     * `first` also subscribes, which starts the `WhileSubscribed` state.
     */
    private suspend fun awaitLanguages(expected: List<LanguageRow>) {
        withTimeout(5_000) { viewModel.state.first { it.languages == expected } }
    }

    private val spanish = LanguageRow(Presets.SPANISH.id, "Castellano", active = true, enabled = true)
    private val english = LanguageRow(Presets.ENGLISH.id, "English", active = false, enabled = true)
    private val noUserLayout = LanguageRow(id = null, name = null, active = false, enabled = false)

    @Test
    fun `the presets come first, castellano is active and usuario is disabled`() = runBlocking {
        awaitLanguages(listOf(spanish, english, noUserLayout))
    }

    @Test
    fun `switching english on makes it the chosen language`() = runBlocking {
        viewModel.onLanguageToggle(Presets.ENGLISH.id)
        awaitLanguages(listOf(spanish.copy(active = false), english.copy(active = true), noUserLayout))
        assertEquals(Presets.ENGLISH.id, storage.settings.characterLayoutId.first())
    }

    @Test
    fun `switching the active language again keeps it on`() = runBlocking {
        viewModel.onLanguageToggle(Presets.SPANISH.id)
        awaitLanguages(listOf(spanish, english, noUserLayout))
    }

    @Test
    fun `usuario is enabled once a user layout exists`() = runBlocking {
        val mine = Presets.SPANISH.copyAsUser(LayoutId("user.es"), "Mi castellano")
        storage.layouts.save(mine)
        awaitLanguages(listOf(spanish, english, LanguageRow(mine.id, "Mi castellano", active = false, enabled = true)))
    }
}
