package pol.rubiano.eyesensekeyboard.keyboard.domain

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
 * 2. **What the key shows**: `label()` in `presentation/KeyView.kt` (compiler error).
 * 3. **What it does to the text**: [typed] in `Typing.kt` (compiler error). If it doesn't
 *    touch the text (voice, UI…), leave it next to the actions that return `this`.
 * 4. **Tests**: in `TypingTest`, a test for its effect, and take it out of
 *    `functions without behaviour don't change the text yet`.
 * 5. **Tests that assume corners are inert**: `KeyboardViewModelTest` checks that the
 *    corners without behaviour don't change the text. Exclude the new one there and test
 *    it through its corner (no compiler error here: it shows up as a failing test).
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

    /** Reads what's written aloud through the speaker. */
    data object Speak : KeyAction {
        override val id = "speak"
    }

    /** Opens the emotion list. The chosen one becomes the message's context ([Emotion]). */
    data object SelectEmotion : KeyAction {
        override val id = "select_emotion"
    }

    companion object {
        val all: List<KeyAction> = listOf(DeleteCharacter, ClearText, Speak, SelectEmotion)

        fun fromId(id: String): KeyAction? = all.firstOrNull { it.id == id }
    }
}
