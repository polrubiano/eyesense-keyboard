package pol.rubiano.eyesensekeyboard.features.keyboardsettings.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.CharacterLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutOrigin
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.repositories.LayoutRepository
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.usecases.ActivateCharacterLayoutUseCase
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.usecases.GetActiveLayoutsUseCase
import javax.inject.Inject

/**
 * One row of the Idioma tab.
 * - [id] is null only for the Usuario row while there's no user layout: nothing to activate.
 * - [name] is null for that same empty row, so the UI shows its own "Usuario" label
 *   (a ViewModel doesn't read Android strings).
 */
data class LanguageRow(val id: LayoutId?, val name: String?, val active: Boolean, val enabled: Boolean)

data class KeyboardSettingsUiState(val languages: List<LanguageRow>)

/**
 * The keyboard settings' state. For now only the Idioma tab: the languages that exist (from the
 * keyboard's [layouts]) with the one in use switched on, and a switch that chooses another.
 *
 * ```
 * layouts.characterLayouts ─┐
 *                           ├─ combine ─► [Castellano] [English] [Usuario]
 * getActiveLayouts() ───────┘               active = the one in use
 * ```
 * There's no "only one on" logic here: there's one active language, so switching English on
 * makes Castellano's switch turn off on its own.
 */
@HiltViewModel
class KeyboardSettingsViewModel @Inject constructor(
    layouts: LayoutRepository,
    getActiveLayouts: GetActiveLayoutsUseCase,
    private val activateCharacterLayout: ActivateCharacterLayoutUseCase,
) : ViewModel() {

    val state: StateFlow<KeyboardSettingsUiState> =
        combine(layouts.characterLayouts, getActiveLayouts()) { all, active -> uiState(all, active.characters) }
            // If storage fails, the presets are still there: the tab stays usable.
            .catch { emit(FALLBACK) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LOADING)

    /**
     * A language's switch. Switching the active one again keeps it on: there's always one
     * active language, so a switch can't turn every language off.
     */
    fun onLanguageToggle(id: LayoutId) {
        viewModelScope.launch { activateCharacterLayout(id) }
    }

    private companion object {
        /**
         * Before storage answers: no rows, rather than a guess. Guessing the presets showed
         * Castellano switched on for an instant when the user had chosen English.
         */
        val LOADING = KeyboardSettingsUiState(languages = emptyList())

        /** If storage fails: the presets with the default one active, so a language can still be chosen. */
        val FALLBACK = uiState(Presets.characterLayouts, Presets.SPANISH)
    }
}

/**
 * The presets first, in their order, then one Usuario row: the first user layout (they come
 * sorted by name), or a disabled row while there's none. There's no layout editor yet, so the
 * panel has room for exactly one.
 */
private fun uiState(layouts: List<CharacterLayout>, active: CharacterLayout): KeyboardSettingsUiState {
    fun row(layout: CharacterLayout) = LanguageRow(layout.id, layout.name, active = layout.id == active.id, enabled = true)

    val presets = layouts.filter { it.origin == LayoutOrigin.PRESET }.map(::row)
    val user = layouts.firstOrNull { it.origin == LayoutOrigin.USER }?.let(::row)
        ?: LanguageRow(id = null, name = null, active = false, enabled = false)
    return KeyboardSettingsUiState(presets + user)
}
