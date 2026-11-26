package pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datasources

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.LayoutLocalDataSource
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.CharacterLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.FunctionLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId

/**
 * User layouts in memory: lost when the app closes. It's for tests (and previews), where a
 * real database isn't needed. Sorted by name, like the database.
 */
class LayoutMemLocalDataSource : LayoutLocalDataSource {

    private val characters = MutableStateFlow<Map<LayoutId, CharacterLayout>>(emptyMap())
    private val functions = MutableStateFlow<Map<LayoutId, FunctionLayout>>(emptyMap())

    override val characterLayouts: Flow<List<CharacterLayout>> =
        characters.map { it.values.sortedBy(CharacterLayout::name) }

    override val functionLayouts: Flow<List<FunctionLayout>> =
        functions.map { it.values.sortedBy(FunctionLayout::name) }

    override suspend fun save(layout: CharacterLayout) = characters.update { it + (layout.id to layout) }

    override suspend fun save(layout: FunctionLayout) = functions.update { it + (layout.id to layout) }

    override suspend fun deleteCharacterLayout(id: LayoutId) = characters.update { it - id }

    override suspend fun deleteFunctionLayout(id: LayoutId) = functions.update { it - id }
}
