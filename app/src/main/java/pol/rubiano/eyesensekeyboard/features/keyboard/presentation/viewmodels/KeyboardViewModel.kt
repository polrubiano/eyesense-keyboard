package pol.rubiano.eyesensekeyboard.features.keyboard.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAction
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAssignment
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Keyboard
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.PanelSlot
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Writing
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.pressed
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.repositories.LayoutRepository
import javax.inject.Inject

/**
 * Holds the writing screen's [state]. It outlives the composable (e.g. when the phone is
 * rotated) and is where side effects will live later on: speaking the text, AI predictions
 * built on it. What a press does to the state isn't decided here: [pressed] decides.
 *
 * The screen draws [state] and reports taps through [onSlotClick].
 *
 * The keyboard is built from the [repository]'s active layouts and follows them: if the active
 * language or functions change, only [Writing.base] is replaced. What's written, the emotion
 * and the spoken messages stay, and an open menu is rebuilt on the new keyboard (it derives
 * from `base`).
 *
 * ```
 * activeCharacterLayout ─┐
 *                        ├─ combine ─► Keyboard(PANEL, …) ─► state.base
 * activeFunctionLayout ──┘
 * ```
 */
@HiltViewModel
class KeyboardViewModel @Inject constructor(
    private val repository: LayoutRepository,
) : ViewModel() {

    // Private and mutable inside, public and read-only outside: only the ViewModel changes it.
    // It starts with the presets so the screen has a keyboard to draw before storage answers
    // (a few milliseconds); the active layouts replace it as soon as they arrive.
    private val _state = MutableStateFlow(
        Writing(Keyboard(Presets.PANEL, Presets.SPANISH, Presets.DEFAULT_FUNCTIONS)),
    )
    val state: StateFlow<Writing> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.activeCharacterLayout, repository.activeFunctionLayout) { characters, functions ->
                Keyboard(Presets.PANEL, characters, functions)
            }.collect { keyboard -> _state.update { it.copy(base = keyboard) } }
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
}
