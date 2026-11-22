package pol.rubiano.eyesensekeyboard.keyboard.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pol.rubiano.eyesensekeyboard.hexgrid.domain.PanelSlot
import pol.rubiano.eyesensekeyboard.keyboard.domain.Keyboard
import pol.rubiano.eyesensekeyboard.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.keyboard.domain.typed

/**
 * State of the writing screen. It outlives the composable (e.g. when the phone is rotated)
 * and is where the asynchronous work will live later on (AI predictions built on [text]).
 *
 * The screen only draws [text] and reports taps through [onSlotClick].
 */
class KeyboardViewModel(
    val keyboard: Keyboard = Keyboard(Presets.PANEL, Presets.SPANISH, Presets.DEFAULT_FUNCTIONS),
) : ViewModel() {

    // Private and mutable inside, public and read-only outside: only the ViewModel changes the text.
    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    fun onSlotClick(slot: PanelSlot) {
        _text.update { current -> current.typed(keyboard.keyAt(slot)) }
    }
}
