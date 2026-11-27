package pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.models

import pol.rubiano.eyesensekeyboard.features.keyboard.domain.CharacterLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.FunctionLayout

/** The layouts the keyboard uses right now: the language and the corner functions. */
data class ActiveLayouts(val characters: CharacterLayout, val functions: FunctionLayout)
