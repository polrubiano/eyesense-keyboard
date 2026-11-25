package pol.rubiano.eyesensekeyboard.keyboard.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import pol.rubiano.eyesensekeyboard.keyboard.domain.CharacterLayout
import pol.rubiano.eyesensekeyboard.keyboard.domain.FunctionLayout
import pol.rubiano.eyesensekeyboard.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.keyboard.domain.LayoutOrigin
import pol.rubiano.eyesensekeyboard.keyboard.domain.LayoutRepository
import pol.rubiano.eyesensekeyboard.keyboard.domain.Presets

/** In-memory implementation: user layouts are lost when the app is closed. */
class InMemoryLayoutRepository(
    private val presetCharacters: List<CharacterLayout> = Presets.characterLayouts,
    private val presetFunctions: List<FunctionLayout> = Presets.functionLayouts,
) : LayoutRepository {

    private val userCharacters = MutableStateFlow<Map<LayoutId, CharacterLayout>>(emptyMap())
    private val userFunctions = MutableStateFlow<Map<LayoutId, FunctionLayout>>(emptyMap())

    override val characterLayouts: Flow<List<CharacterLayout>> =
        userCharacters.map { users -> presetCharacters + users.values }

    override val functionLayouts: Flow<List<FunctionLayout>> =
        userFunctions.map { users -> presetFunctions + users.values }

    override suspend fun save(layout: CharacterLayout) {
        checkWritable(layout.id, layout.origin, presetCharacters.map { it.id })
        userCharacters.update { it + (layout.id to layout) }
    }

    override suspend fun save(layout: FunctionLayout) {
        checkWritable(layout.id, layout.origin, presetFunctions.map { it.id })
        userFunctions.update { it + (layout.id to layout) }
    }

    override suspend fun deleteCharacterLayout(id: LayoutId) {
        userCharacters.update { it - id }
    }

    override suspend fun deleteFunctionLayout(id: LayoutId) {
        userFunctions.update { it - id }
    }

    private fun checkWritable(id: LayoutId, origin: LayoutOrigin, presetIds: List<LayoutId>) {
        require(origin == LayoutOrigin.USER) { "Only user layouts can be saved" }
        require(id !in presetIds) { "The id ${id.value} belongs to a preset and can't be overwritten" }
    }
}
