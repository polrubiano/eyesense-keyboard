package pol.rubiano.eyesensekeyboard.features.keyboard.domain

/**
 * A set of [options] the keyboard turns into while it's open. It's opened (and closed)
 * by the function key that holds [opener].
 *
 * It's `sealed` for the same reason as [Option]: "none" removes **this menu's** value, and
 * the code that does it uses a `when` over the menus, with no `else`.
 */
sealed interface Menu {
    val opener: KeyAction
    val options: List<Option>

    data object Emotions : Menu {
        override val opener = KeyAction.SelectEmotion
        // The order the user gave: happy, sad, angry.
        override val options: List<Option> = listOf(Emotion.HAPPY, Emotion.SAD, Emotion.ANGRY)
    }
}

/**
 * The menu a function opens, or null if it does something else. No `else`: a new action
 * has to decide whether it opens a menu. It must match [Menu.opener] (MenuTest checks it).
 */
fun KeyAction.menu(): Menu? = when (this) {
    KeyAction.SelectEmotion -> Menu.Emotions
    KeyAction.DeleteCharacter,
    KeyAction.ClearText,
    KeyAction.Speak,
    KeyAction.OpenSettings -> null
}

/**
 * The keyboard while [menu] is open. Every menu follows the same rule:
 *
 *
 * centre                    → Choice(null)     "none": removes the value and closes
 * then the spiral, in order → Choice(option)   stores it and closes
 * corners                   → only [Menu.opener]'s, wherever the user put it: closes
 *
 *
 * The options take the hexagons in the panel's spiral order
 * ([HexPanel.cells][pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexPanel.cells]), the same
 * one the letters use: nearest to the centre first. Nothing is merged, so the space bar
 * splits back into hexagons, and every free hexagon is drawn empty.
 */
fun Keyboard.showing(menu: Menu): Keyboard {
    val choices = listOf(KeyAssignment.Choice(null)) + menu.options.map { KeyAssignment.Choice(it) }
    require(choices.size <= panel.cells.size) {
        "${menu.options.size} options + none don't fit in the panel's ${panel.cells.size} hexagons"
    }
    val id = LayoutId("menu.${menu.opener.id}")
    return withCharacters(
        CharacterLayout(id, menu.toString(), LayoutOrigin.PRESET, panel.cells.zip(choices).toMap()),
    ).withFunctions(
        FunctionLayout(id, menu.toString(), LayoutOrigin.PRESET, functions.corners.filterValues { it == menu.opener }),
    )
}

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

/**
 * Emotion attached to the message. It is **context, not text**: it isn't written into the field,
 * it travels alongside the message (in the future, together with the prompt sent to the AI).
 *
 * Today the user picks it with [KeyAction.SelectEmotion]; later EyeSense's real-time
 * detection will be able to fill in this same value.
 *
 * [id] is stable because it's a value that will be sent to other systems.
 *
 * It's an [Option]: the user picks it from [Menu.Emotions]. "No emotion" is `null`,
 * not a value of the enum, because "none" isn't an emotion to send anywhere.
 */
enum class Emotion(val id: String, override val pictogram: String) : Option {
    HAPPY("happy", "😊"),
    ANGRY("angry", "😠"),
    SAD("sad", "😢"),
    ;

    companion object {
        fun fromId(id: String): Emotion? = entries.firstOrNull { it.id == id }
    }
}
