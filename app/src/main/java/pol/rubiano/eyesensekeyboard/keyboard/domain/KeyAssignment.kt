package pol.rubiano.eyesensekeyboard.keyboard.domain

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
