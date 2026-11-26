package pol.rubiano.eyesensekeyboard.features.keyboard.domain.repositories

import kotlinx.coroutines.flow.Flow
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.CharacterLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.FunctionLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId

/**
 * Where layouts are stored: the presets + the user's own, and which of them are in use.
 *
 * It's asynchronous (`Flow` and `suspend`) because storage is: the layouts live in a
 * database, and reading or writing it can't block the screen. The `Flow`s emit again
 * whenever what they show changes.
 */
interface LayoutRepository {
    val characterLayouts: Flow<List<CharacterLayout>>
    val functionLayouts: Flow<List<FunctionLayout>>

    /**
     * The layouts the keyboard uses. There's always one: the activated layout while it exists,
     * otherwise the preset (nothing activated yet, or the activated one was deleted).
     */
    val activeCharacterLayout: Flow<CharacterLayout>
    val activeFunctionLayout: Flow<FunctionLayout>

    /** Puts the layout with [id] in use. It must exist (preset or user). */
    suspend fun activateCharacterLayout(id: LayoutId)
    suspend fun activateFunctionLayout(id: LayoutId)

    /** Saves (or replaces) a user layout. Presets are read-only. */
    suspend fun save(layout: CharacterLayout)
    suspend fun save(layout: FunctionLayout)

    suspend fun deleteCharacterLayout(id: LayoutId)
    suspend fun deleteFunctionLayout(id: LayoutId)
}
