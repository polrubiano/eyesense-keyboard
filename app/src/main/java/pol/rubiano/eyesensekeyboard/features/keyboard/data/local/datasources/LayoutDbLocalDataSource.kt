package pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datasources

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.LayoutLocalDataSource
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.daos.LayoutDao
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.db.mappers.toDomain
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.db.mappers.toEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.CharacterLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.FunctionLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId

/** User layouts in the Room database: the DAO stores rows, the mappers translate them. */
class LayoutDbLocalDataSource(private val dao: LayoutDao) : LayoutLocalDataSource {

    override val characterLayouts: Flow<List<CharacterLayout>> =
        dao.characterLayouts().map { rows -> rows.map { it.toDomain() } }

    override val functionLayouts: Flow<List<FunctionLayout>> =
        dao.functionLayouts().map { rows -> rows.map { it.toDomain() } }

    override suspend fun save(layout: CharacterLayout) = dao.save(layout.toEntity())

    override suspend fun save(layout: FunctionLayout) = dao.save(layout.toEntity())

    override suspend fun deleteCharacterLayout(id: LayoutId) = dao.deleteCharacterLayout(id.value)

    override suspend fun deleteFunctionLayout(id: LayoutId) = dao.deleteFunctionLayout(id.value)
}
