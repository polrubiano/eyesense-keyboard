package pol.rubiano.eyesensekeyboard.keyboard.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pol.rubiano.eyesensekeyboard.hexgrid.domain.PanelSlot
import pol.rubiano.eyesensekeyboard.keyboard.domain.Emotion
import pol.rubiano.eyesensekeyboard.keyboard.domain.KeyAssignment
import pol.rubiano.eyesensekeyboard.keyboard.domain.Keyboard
import pol.rubiano.eyesensekeyboard.keyboard.domain.Menu
import pol.rubiano.eyesensekeyboard.keyboard.domain.Option
import pol.rubiano.eyesensekeyboard.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.keyboard.domain.menu
import pol.rubiano.eyesensekeyboard.keyboard.domain.showing
import pol.rubiano.eyesensekeyboard.keyboard.domain.typed

/**
 * State of the writing screen. It outlives the composable (e.g. when the phone is rotated)
 * and is where the asynchronous work will live later on (AI predictions built on [text]).
 *
 * The screen draws [text] and [keyboard], and reports taps through [onSlotClick].
 * [emotion] is the message's context: it isn't drawn as text, it travels with the message.
 */
class KeyboardViewModel(
    private val base: Keyboard = Keyboard(Presets.PANEL, Presets.SPANISH, Presets.DEFAULT_FUNCTIONS),
) : ViewModel() {

    // Private and mutable inside, public and read-only outside: only the ViewModel changes them.
    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _emotion = MutableStateFlow<Emotion?>(null)
    val emotion: StateFlow<Emotion?> = _emotion.asStateFlow()

    /** The keyboard on screen: [base], or the open menu built on top of it. */
    private val _keyboard = MutableStateFlow(base)
    val keyboard: StateFlow<Keyboard> = _keyboard.asStateFlow()

    /** Null = normal keyboard. */
    private var openMenu: Menu? = null

    /**
     * Two separate questions for every tap:
     * 1. What happens to the text? [typed] decides, as before (choices and menus don't write).
     * 2. Does it have an effect beyond the text? Opening/closing a menu, storing an option.
     */
    fun onSlotClick(slot: PanelSlot) {
        val key = _keyboard.value.keyAt(slot)
        _text.update { it.typed(key) }
        when (key) {
            // The opener toggles: opens its menu, or closes it if it's already open.
            is KeyAssignment.Function -> key.action.menu()?.let { show(if (it == openMenu) null else it) }
            // Picking is implicit: store it and close, with no confirmation.
            is KeyAssignment.Choice -> {
                store(key.option)
                show(null)
            }
            is KeyAssignment.Text, null -> Unit
        }
    }

    /** One place changes the menu, so [openMenu] and [keyboard] never disagree. */
    private fun show(menu: Menu?) {
        openMenu = menu
        _keyboard.value = menu?.let { base.showing(it) } ?: base
    }

    /**
     * Where each option is stored. Both `when`s have no `else`: a new kind of [Option] or a
     * new [Menu] breaks the build here until you decide where its value lives.
     */
    private fun store(option: Option?) {
        when (option) {
            is Emotion -> _emotion.value = option
            // "None" removes the value of the menu that's open.
            null -> when (checkNotNull(openMenu) { "A choice was picked with no menu open" }) {
                Menu.Emotions -> _emotion.value = null
            }
        }
    }
}
