package pol.rubiano.eyesensekeyboard.features.keyboard.domain

/**
 * What a key holds:
 * - [Text]: **anything that writes**. A letter ("e"), the space (" ") or a whole
 *   phrase ("¿qué tal?").
 * - [Function]: anything that **does** something instead of writing ([KeyAction]).
 * - [Choice]: an option of an open [Menu]. Picking it stores it and closes the menu.
 */
sealed interface KeyAssignment {

    /** Letters are stored in lowercase; showing them in uppercase is the UI's job. */
    data class Text(val value: String) : KeyAssignment {
        init {
            require(value.isNotEmpty()) { "A text key can't be empty" }
        }
    }

    data class Function(val action: KeyAction) : KeyAssignment

    /**
     * It doesn't know which menu it belongs to, only that it holds an [Option].
     * [option] is null for "none": the centre of every menu, which removes the value.
     */
    data class Choice(val option: Option?) : KeyAssignment
}

/**
 * Catalogue of functions a key can trigger.
 *
 * Each one has a stable [id]: it's what will get saved when user layouts are persisted,
 * so it must never change, even if the class gets renamed.
 *
 * ## Adding a function
 *
 * Every `when` over [KeyAction] lists all the cases with no `else`, on purpose. Add the new
 * case and the build breaks in each place that has to decide something about it. Each
 * compiler error is a breadcrumb:
 *
 * 1. **Here**: add a `data object` with a new, unique [id], and add it to [all].
 * 2. **What the key shows**: in `presentation/components/KeyView.kt` (compiler errors):
 *    - `icon()`: create the drawable first (File > New > Vector Asset, 24 dp).
 *    - `description()`: add the text TalkBack reads to `strings.xml`.
 * 3. **What it does to the text**: [typed] in `Writing.kt` (compiler error). If it doesn't
 *    touch the text (voice, UI…), leave it next to the actions that return `this`.
 * 4. **Does it open a menu?**: `menu()` in `Menu.kt` (compiler error). Most actions don't: `null`.
 *    [pressed] already opens and closes it.
 * 5. **Does it change any other state?** (no compiler error): add the value to `Writing`
 *    and give the action its own branch in [pressed]. It stays pure: state in, state out.
 * 6. **Does it act on the outside world?** (sound, network…; no compiler error): that's a
 *    side effect, not state. It goes in `KeyboardViewModel.onSlotClick`, after [pressed],
 *    never in the domain.
 * 7. **Tests**: in `WritingTest`, a test for what it does to the state, and take it out of
 *    `functions without behaviour don't change the text yet`. A side effect is tested in
 *    `KeyboardViewModelTest`.
 *
 * Optional: put it in a corner by default in `Presets.DEFAULT_FUNCTIONS`.
 *
 * Example to follow: [DeleteCharacter].
 */
sealed interface KeyAction {
    val id: String

    /** Deletes the last character. */
    data object DeleteCharacter : KeyAction {
        override val id = "delete_character"
    }

    /** Clears everything written. Destructive: it'll need protection against accidental activation. */
    data object ClearText : KeyAction {
        override val id = "clear_text"
    }

    /**
     * Says what's written and leaves the field ready for the next message.
     * For now it only shows it on screen ([SpokenMessage.payload]); reading it aloud comes later.
     */
    data object Speak : KeyAction {
        override val id = "speak"
    }

    /** Opens the emotion list. The chosen one becomes the message's context ([Emotion]). */
    data object SelectEmotion : KeyAction {
        override val id = "select_emotion"
    }

    /**
     * Opens the keyboard settings in the keyboard's place, and closes them if they're open
     * ([Writing.settingsOpen]).
     */
    data object OpenSettings : KeyAction {
        override val id = "open_settings"
    }

    companion object {
        val all: List<KeyAction> = listOf(DeleteCharacter, ClearText, Speak, SelectEmotion, OpenSettings)

        fun fromId(id: String): KeyAction? = all.firstOrNull { it.id == id }
    }
}
