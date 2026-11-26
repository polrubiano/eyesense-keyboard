package pol.rubiano.eyesensekeyboard.features.keyboard.data.local

import kotlinx.coroutines.flow.Flow
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.CharacterLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.FunctionLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId

/**
 * Where the **user's** layouts are kept on this device. It only stores and returns them, and
 * applies no rules: whether something may be saved is decided by
 * [LayoutDataRepository][pol.rubiano.eyesensekeyboard.features.keyboard.data.datarepositories.LayoutDataRepository].
 *
 * Two implementations: the database (the real one) and memory (for tests). Both list
 * layouts sorted by name, so they behave the same.
 */
interface LayoutLocalDataSource {
    val characterLayouts: Flow<List<CharacterLayout>>
    val functionLayouts: Flow<List<FunctionLayout>>

    /** Saves the layout, or replaces the one with the same id. */
    suspend fun save(layout: CharacterLayout)
    suspend fun save(layout: FunctionLayout)

    /** Deleting an id that isn't stored does nothing. */
    suspend fun deleteCharacterLayout(id: LayoutId)
    suspend fun deleteFunctionLayout(id: LayoutId)
}
