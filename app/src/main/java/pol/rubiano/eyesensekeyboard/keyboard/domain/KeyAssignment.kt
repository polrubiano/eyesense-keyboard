package pol.rubiano.eyesensekeyboard.keyboard.domain

/**
 * What a key holds. Only two things:
 * - [Text]: **anything that writes**. A letter ("e"), the space (" ") or a whole
 *   phrase ("¿qué tal?").
 * - [Function]: anything that **does** something instead of writing ([KeyAction]).
 */
sealed interface KeyAssignment {

    /** Letters are stored in lowercase; showing them in uppercase is the UI's job. */
    data class Text(val value: String) : KeyAssignment {
        init {
            require(value.isNotEmpty()) { "A text key can't be empty" }
        }
    }

    data class Function(val action: KeyAction) : KeyAssignment
}
