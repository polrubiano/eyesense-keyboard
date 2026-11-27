package pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.repositories

import kotlinx.coroutines.flow.Flow
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId

/**
 * The user's choices for the keyboard: the single door to read and change them, from anywhere.
 *
 * It stores **choices**, not layouts: which language and which corner functions are chosen, as
 * ids. What each layout looks like belongs to the keyboard
 * ([LayoutRepository][pol.rubiano.eyesensekeyboard.features.keyboard.domain.repositories.LayoutRepository]).
 * The rules that join both (only an existing layout can be chosen; a missing one falls back to
 * the preset) live in the use cases next to this package.
 */
interface KeyboardSettingsRepository {
    /** Null until a language has been chosen. */
    val characterLayoutId: Flow<LayoutId?>

    /** Null until a function layout has been chosen. */
    val functionLayoutId: Flow<LayoutId?>

    suspend fun setCharacterLayoutId(id: LayoutId)
    suspend fun setFunctionLayoutId(id: LayoutId)
}
