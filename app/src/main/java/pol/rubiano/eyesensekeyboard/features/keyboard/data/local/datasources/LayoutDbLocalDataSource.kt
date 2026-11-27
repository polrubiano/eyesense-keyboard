package pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datasources

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.daos.LayoutDao
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.db.mappers.toDomain
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.db.mappers.toEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.CharacterLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.FunctionLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import javax.inject.Inject

/**
 * The **user's** layouts in the Room database: the DAO stores rows, the mappers translate them.
 * Lists come sorted by name (the DAO's query does it).
 *
 * It only stores and returns them, and applies no rules: whether something may be saved is
 * decided by
 * [LayoutDataRepository][pol.rubiano.eyesensekeyboard.features.keyboard.data.datarepositories.LayoutDataRepository].
 *
 * It's a plain class, with no interface, because it's the only implementation: tests use it
 * too, with an in-memory database.
 */
class LayoutDbLocalDataSource @Inject constructor(private val dao: LayoutDao) {

    val characterLayouts: Flow<List<CharacterLayout>> =
        dao.characterLayouts().map { rows -> rows.map { it.toDomain() } }

    val functionLayouts: Flow<List<FunctionLayout>> =
        dao.functionLayouts().map { rows -> rows.map { it.toDomain() } }

    /** Saves the layout, or replaces the one with the same id. */
    suspend fun save(layout: CharacterLayout) = dao.save(layout.toEntity())

    suspend fun save(layout: FunctionLayout) = dao.save(layout.toEntity())

    /** Deleting an id that isn't stored does nothing. */
    suspend fun deleteCharacterLayout(id: LayoutId) = dao.deleteCharacterLayout(id.value)

    suspend fun deleteFunctionLayout(id: LayoutId) = dao.deleteFunctionLayout(id.value)
}
