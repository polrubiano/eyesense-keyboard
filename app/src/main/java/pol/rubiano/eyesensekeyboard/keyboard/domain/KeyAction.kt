package pol.rubiano.eyesensekeyboard.keyboard.domain

/**
 * Catalogue of functions a key can trigger.
 *
 * Each one has a stable [id]: it's what will get saved when user layouts are persisted,
 * so it must never change, even if the class gets renamed.
 *
 * Adding a function = adding a case here (and to [all]). Because it's `sealed`, the compiler
 * will point out every `when` that needs to handle it.
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
