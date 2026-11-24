package pol.rubiano.eyesensekeyboard.keyboard.domain

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
    KeyAction.Speak -> null
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
 * The options take the hexagons in the panel's spiral order ([HexPanel.cells]), the same
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
