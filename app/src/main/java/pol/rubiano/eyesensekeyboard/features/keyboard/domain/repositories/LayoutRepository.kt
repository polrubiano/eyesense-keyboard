package pol.rubiano.eyesensekeyboard.features.keyboard.domain.repositories

import kotlinx.coroutines.flow.Flow
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.CharacterLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.FunctionLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId

/**
 * Where layouts are stored: the presets + the user's own. A layout says what a language (or a
 * set of corner functions) looks like; which one is in use is the user's choice, a keyboard
 * setting that lives in `keyboardsettings`.
 *
 * It's asynchronous (`Flow` and `suspend`) because storage is: the layouts live in a
 * database, and reading or writing it can't block the screen. The `Flow`s emit again
 * whenever what they show changes.
 */
interface LayoutRepository {
    val characterLayouts: Flow<List<CharacterLayout>>
    val functionLayouts: Flow<List<FunctionLayout>>

    /** Saves (or replaces) a user layout. Presets are read-only. */
    suspend fun save(layout: CharacterLayout)
    suspend fun save(layout: FunctionLayout)

    suspend fun deleteCharacterLayout(id: LayoutId)
    suspend fun deleteFunctionLayout(id: LayoutId)
}
