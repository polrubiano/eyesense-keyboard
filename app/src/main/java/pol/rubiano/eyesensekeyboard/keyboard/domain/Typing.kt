package pol.rubiano.eyesensekeyboard.keyboard.domain

/**
 * The text after pressing [key].
 *
 * - [KeyAssignment.Text] → its value goes at the end (a letter, the space or a whole phrase).
 * - [KeyAssignment.Function] → no change yet: functions get their behaviour later,
 *   and this is the place where it will be plugged in.
 * - `null` (free slot) → no change.
 *
 * It's a pure function: same input, same output, no Android involved. That's why the
 * ViewModel can delegate to it and it can be tested on its own.
 */
fun String.typed(key: KeyAssignment?): String = when (key) {
    is KeyAssignment.Text -> this + key.value
    is KeyAssignment.Function -> this
    null -> this
}
