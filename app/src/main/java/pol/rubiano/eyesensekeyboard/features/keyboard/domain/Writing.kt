package pol.rubiano.eyesensekeyboard.features.keyboard.domain

/**
 * Everything the writing screen is about, in **one immutable value**: what's written, the
 * message's emotion, which menu is open (if any) and the messages said so far.
 *
 * [keyboard] isn't stored, it's derived from [openMenu]. So the menu and the keyboard on
 * screen can never disagree: there's no second variable to forget to update.
 *
 * A press doesn't change it, it produces a new one ([pressed]).
 */
data class Writing(
    val base: Keyboard,
    val text: String = "",
    val emotion: Emotion? = null,
    val openMenu: Menu? = null,
    /** Oldest first. Temporary: it lets the screen show what Speak would say, until it has a voice. */
    val spoken: List<SpokenMessage> = emptyList(),
) {
    /** The keyboard on screen: [base], or the open menu built on top of it. */
    val keyboard: Keyboard = openMenu?.let { base.showing(it) } ?: base
}

/**
 * A message that was said: the text, together with the emotion it carried at that moment.
 * It's a snapshot: changing the emotion afterwards doesn't change what was already said.
 */
data class SpokenMessage(val emotion: Emotion?, val text: String) {

    /**
     * What predictions will receive: `[happy][hola]`, or `[][hola]` with no emotion.
     * It uses [Emotion.id], not the pictogram: the emoji is for people, the id for other systems.
     */
    val payload: String get() = "[${emotion?.id.orEmpty()}][$text]"
}

/**
 * The state after pressing [key]. Two separate questions for every press:
 * 1. What happens to the text? [typed] decides (choices and menus don't write).
 * 2. What else changes? Opening/closing a menu, storing an option, keeping what was said.
 *
 * It's a pure function, like [typed]: same state + same key → same new state. Side
 * effects (sound, AI requests…) don't belong here: whoever calls it runs them.
 */
fun Writing.pressed(key: KeyAssignment?): Writing {
    val typed = copy(text = text.typed(key))
    return when (key) {
        is KeyAssignment.Function -> when {
            // Speaking keeps the message (with the emotion it carried) and [typed] empties the
            // field. The emotion stays for the next message. An empty field has nothing to say.
            key.action == KeyAction.Speak ->
                if (text.isEmpty()) this else typed.copy(spoken = spoken + SpokenMessage(emotion, text))
            // The opener toggles: opens its menu, or closes it if it's already open.
            else -> when (val menu = key.action.menu()) {
                null -> typed
                else -> typed.copy(openMenu = if (menu == openMenu) null else menu)
            }
        }
        // Picking is implicit: store it and close, with no confirmation.
        is KeyAssignment.Choice -> typed.storing(key.option).copy(openMenu = null)
        is KeyAssignment.Text, null -> typed
    }
}

/**
 * Where each option is stored. Both `when`s have no `else`: a new kind of [Option] or a
 * new [Menu] breaks the build here until you decide where its value lives.
 */
private fun Writing.storing(option: Option?): Writing = when (option) {
    is Emotion -> copy(emotion = option)
    // "None" removes the value of the menu that's open. With no menu open there's no
    // value to remove: that's a bug in the caller, so it fails right away.
    null -> when (checkNotNull(openMenu) { "A choice was picked with no menu open" }) {
        Menu.Emotions -> copy(emotion = null)
    }
}

/**
 * The text after pressing [key].
 *
 * - [KeyAssignment.Text] → its value goes at the end (a letter, the space or a whole phrase).
 * - [KeyAssignment.Function] → depends on the action. The inner `when` lists every
 *   [KeyAction] with no `else` on purpose: adding a new action breaks the build here
 *   until you decide what it does to the text.
 * - [KeyAssignment.Choice] → no change: picking a menu option isn't writing.
 * - `null` (free slot) → no change.
 *
 * It's a pure function: same input, same output, no Android involved. That's why
 * [pressed] can delegate to it and it can be tested on its own.
 */
fun String.typed(key: KeyAssignment?): String = when (key) {
    is KeyAssignment.Text -> this + key.value
    is KeyAssignment.Function -> when (key.action) {
        // Removes the last character. On an empty text it stays empty.
        KeyAction.DeleteCharacter -> dropLast(1)
        // Wipes everything written. On an empty text it stays empty.
        KeyAction.ClearText -> ""
        // What was said is kept by [pressed]; the field is left empty for the next message.
        KeyAction.Speak -> ""
        // Opens a menu, doesn't write.
        KeyAction.SelectEmotion -> this
    }
    // Neither writes: picking an option is stored by [pressed], and a free slot does nothing.
    is KeyAssignment.Choice, null -> this
}
