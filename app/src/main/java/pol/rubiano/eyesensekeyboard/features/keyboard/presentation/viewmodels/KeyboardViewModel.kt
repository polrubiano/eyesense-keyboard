package pol.rubiano.eyesensekeyboard.features.keyboard.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAction
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAssignment
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Keyboard
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.PanelSlot
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Writing
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.pressed
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.usecases.GetActiveLayoutsUseCase
import javax.inject.Inject

/**
 * Holds the writing screen's [state]. It outlives the composable (e.g. when the phone is
 * rotated) and is where side effects will live later on: speaking the text, AI predictions
 * built on it. What a press does to the state isn't decided here: [pressed] decides.
 *
 * The screen draws [state] and reports taps through [onSlotClick].
 *
 * The keyboard is built from the active layouts ([getActiveLayouts]: the user's choice, or the
 * presets) and follows them: if the chosen language or functions change, only [Writing.base]
 * is replaced. What's written, the emotion and the spoken messages stay, and an open menu is
 * rebuilt on the new keyboard (it derives from `base`).
 *
 * ```
 * getActiveLayouts() ─► Keyboard(PANEL, characters, functions) ─► state.base
 * ```
 */
@HiltViewModel
class KeyboardViewModel @Inject constructor(
    getActiveLayouts: GetActiveLayoutsUseCase,
) : ViewModel() {

    // Private and mutable inside, public and read-only outside: only the ViewModel changes it.
    // `Writing` needs a keyboard from the start, so it begins with the presets. Nobody sees
    // them: the splash stays until the active layouts arrive ([isReady]).
    private val _state = MutableStateFlow(
        Writing(Keyboard(Presets.PANEL, Presets.SPANISH, Presets.DEFAULT_FUNCTIONS)),
    )
    val state: StateFlow<Writing> = _state.asStateFlow()

    private val _isReady = MutableStateFlow(false)

    /**
     * True once the keyboard is built from the user's chosen layouts. Until then, `MainActivity`
     * keeps the splash on screen, so the presets are never shown for an instant before the
     * chosen language (Castellano → English).
     */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            getActiveLayouts()
                .map { active -> Keyboard(Presets.PANEL, active.characters, active.functions) }
                // If storage fails, the presets keyboard stays: better than a splash forever.
                .catch { _isReady.value = true }
                .collect { keyboard ->
                    _state.update { it.copy(base = keyboard) }
                    _isReady.value = true
                }
        }
    }

    /** The tap is resolved on the keyboard that's on screen right now: the normal one or a menu. */
    fun onSlotClick(slot: PanelSlot) {
        _state.update { it.pressed(it.keyboard.keyAt(slot)) }
    }

    /**
     * The field's clear button. It goes through [pressed] with the same action as a clear key,
     * so both follow one rule, kept in the domain.
     */
    fun onClearText() {
        _state.update { it.pressed(KeyAssignment.Function(KeyAction.ClearText)) }
    }

    /** The settings' close hexagon: the keyboard comes back as it was. */
    fun onCloseSettings() {
        _state.update { it.copy(settingsOpen = false) }
    }
}
