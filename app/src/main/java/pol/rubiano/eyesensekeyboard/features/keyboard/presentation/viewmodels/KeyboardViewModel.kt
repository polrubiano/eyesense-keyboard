package pol.rubiano.eyesensekeyboard.features.keyboard.presentation.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Keyboard
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.PanelSlot
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Writing
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.pressed

/**
 * Holds the writing screen's [state]. It outlives the composable (e.g. when the phone is
 * rotated) and is where side effects will live later on: speaking the text, AI predictions
 * built on it. What a press does to the state isn't decided here: [pressed] decides.
 *
 * The screen draws [state] and reports taps through [onSlotClick].
 */
class KeyboardViewModel(
    base: Keyboard = Keyboard(Presets.PANEL, Presets.SPANISH, Presets.DEFAULT_FUNCTIONS),
) : ViewModel() {

    // Private and mutable inside, public and read-only outside: only the ViewModel changes it.
    private val _state = MutableStateFlow(Writing(base))
    val state: StateFlow<Writing> = _state.asStateFlow()

    /** The tap is resolved on the keyboard that's on screen right now: the normal one or a menu. */
    fun onSlotClick(slot: PanelSlot) {
        _state.update { it.pressed(it.keyboard.keyAt(slot)) }
    }
}
