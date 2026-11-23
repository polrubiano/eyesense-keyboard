package pol.rubiano.eyesensekeyboard.keyboard.domain

/**
 * The text after pressing [key].
 *
 * - [KeyAssignment.Text] → its value goes at the end (a letter, the space or a whole phrase).
 * - [KeyAssignment.Function] → depends on the action. The inner `when` lists every
 *   [KeyAction] with no `else` on purpose: adding a new action breaks the build here
 *   until you decide what it does to the text.
 * - `null` (free slot) → no change.
 *
 * It's a pure function: same input, same output, no Android involved. That's why the
 * ViewModel can delegate to it and it can be tested on its own.
 */
fun String.typed(key: KeyAssignment?): String = when (key) {
    is KeyAssignment.Text -> this + key.value
    is KeyAssignment.Function -> when (key.action) {
        // Removes the last character. On an empty text it stays empty.
        KeyAction.DeleteCharacter -> dropLast(1)
        // No behaviour yet.
        KeyAction.ClearText,
        KeyAction.Speak,
        KeyAction.SelectEmotion -> this
    }
    null -> this
}
