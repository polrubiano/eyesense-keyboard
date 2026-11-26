package pol.rubiano.eyesensekeyboard.features.keyboard.data.datarepositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.LayoutLocalDataSource
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.CharacterLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.FunctionLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutOrigin
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.repositories.LayoutRepository
import javax.inject.Inject

/**
 * Every layout the app knows: the presets (in code) first, then the user's (from [local]).
 *
 * The rules live here, not in the data source, so they hold wherever layouts are stored:
 * - only user layouts are saved: a preset is changed by copying it (`copyAsUser`);
 * - a user layout can't take a preset's id, or it would hide the preset.
 */
class LayoutDataRepository @Inject constructor(
    private val local: LayoutLocalDataSource,
) : LayoutRepository {

    private val presetCharacters: List<CharacterLayout> = Presets.characterLayouts
    private val presetFunctions: List<FunctionLayout> = Presets.functionLayouts

    override val characterLayouts: Flow<List<CharacterLayout>> =
        local.characterLayouts.map { users -> presetCharacters + users }

    override val functionLayouts: Flow<List<FunctionLayout>> =
        local.functionLayouts.map { users -> presetFunctions + users }

    override suspend fun save(layout: CharacterLayout) {
        checkWritable(layout.id, layout.origin, presetCharacters.map { it.id })
        local.save(layout)
    }

    override suspend fun save(layout: FunctionLayout) {
        checkWritable(layout.id, layout.origin, presetFunctions.map { it.id })
        local.save(layout)
    }

    override suspend fun deleteCharacterLayout(id: LayoutId) = local.deleteCharacterLayout(id)

    override suspend fun deleteFunctionLayout(id: LayoutId) = local.deleteFunctionLayout(id)

    private fun checkWritable(id: LayoutId, origin: LayoutOrigin, presetIds: List<LayoutId>) {
        require(origin == LayoutOrigin.USER) { "Only user layouts can be saved" }
        require(id !in presetIds) { "The id ${id.value} belongs to a preset and can't be overwritten" }
    }
}
