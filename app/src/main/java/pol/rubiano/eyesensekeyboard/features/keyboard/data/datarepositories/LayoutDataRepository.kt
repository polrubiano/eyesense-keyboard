package pol.rubiano.eyesensekeyboard.features.keyboard.data.datarepositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datasources.LayoutDbLocalDataSource
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datastore.ActiveLayoutDataStoreManager
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.CharacterLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.FunctionLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutOrigin
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.repositories.LayoutRepository
import javax.inject.Inject

/**
 * Every layout the app knows: the presets (in code) first, then the user's (from [local]).
 * Which ones are in use comes from [active].
 *
 * The rules live here, not in the data sources, so they hold wherever things are stored:
 * - only user layouts are saved: a preset is changed by copying it (`copyAsUser`);
 * - a user layout can't take a preset's id, or it would hide the preset;
 * - only an existing layout can be activated;
 * - there's always an active layout: if the stored id points to nothing, the preset is used.
 *
 * ```
 * characterLayouts ─┐
 *                   ├─ combine ─► the one with the active id, or Presets.SPANISH
 * active id ────────┘
 * ```
 * Because the active layout is derived from the list, deleting it needs no extra code: the
 * list changes, and the active layout falls back to the preset on its own.
 */
class LayoutDataRepository @Inject constructor(
    private val local: LayoutDbLocalDataSource,
    private val active: ActiveLayoutDataStoreManager,
) : LayoutRepository {

    private val presetCharacters: List<CharacterLayout> = Presets.characterLayouts
    private val presetFunctions: List<FunctionLayout> = Presets.functionLayouts

    override val characterLayouts: Flow<List<CharacterLayout>> =
        local.characterLayouts.map { users -> presetCharacters + users }

    override val functionLayouts: Flow<List<FunctionLayout>> =
        local.functionLayouts.map { users -> presetFunctions + users }

    override val activeCharacterLayout: Flow<CharacterLayout> =
        combine(characterLayouts, active.characterLayoutId) { layouts, id ->
            layouts.firstOrNull { it.id == id } ?: Presets.SPANISH
        }.distinctUntilChanged()

    override val activeFunctionLayout: Flow<FunctionLayout> =
        combine(functionLayouts, active.functionLayoutId) { layouts, id ->
            layouts.firstOrNull { it.id == id } ?: Presets.DEFAULT_FUNCTIONS
        }.distinctUntilChanged()

    override suspend fun activateCharacterLayout(id: LayoutId) {
        require(characterLayouts.first().any { it.id == id }) { "There's no character layout with id ${id.value}" }
        active.setCharacterLayoutId(id)
    }

    override suspend fun activateFunctionLayout(id: LayoutId) {
        require(functionLayouts.first().any { it.id == id }) { "There's no function layout with id ${id.value}" }
        active.setFunctionLayoutId(id)
    }

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
