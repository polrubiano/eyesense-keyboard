package pol.rubiano.eyesensekeyboard.keyboard.domain

/**
 * Something that can be picked in a [Menu]. A key holds it through [KeyAssignment.Choice].
 *
 * It's `sealed` on purpose: whoever stores the picked option uses a `when` with no `else`,
 * so adding a new kind of option (e.g. a language) breaks the build there until you
 * decide where it gets stored.
 *
 * A sealed interface can only be implemented in its own package, and that's fine: the
 * options are domain values, like [Emotion].
 */
sealed interface Option {
    /**
     * What its key shows: an emoji. It belongs to the option, not to the UI, because
     * future options (whole phrases the user creates) will be data, and their pictogram
     * gets saved with them. An emoji is text, so it needs no drawable, and TalkBack reads it.
     */
    val pictogram: String
}
