package pol.rubiano.eyesensekeyboard.keyboard.domain

import kotlinx.coroutines.flow.Flow

/**
 * Where layouts are stored: the presets + the user's own.
 *
 * It's asynchronous (`Flow` and `suspend`) even though the current implementation is
 * in-memory: real persistence (DataStore) is asynchronous, so this way it can be swapped
 * in later without changing anyone who uses the interface.
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
