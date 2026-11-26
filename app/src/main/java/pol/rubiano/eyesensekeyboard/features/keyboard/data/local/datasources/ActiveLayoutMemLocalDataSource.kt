package pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datasources

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.ActiveLayoutLocalDataSource
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId

/** The active layout ids in memory: lost when the app closes. For tests and previews. */
class ActiveLayoutMemLocalDataSource : ActiveLayoutLocalDataSource {

    private val characters = MutableStateFlow<LayoutId?>(null)
    private val functions = MutableStateFlow<LayoutId?>(null)

    override val characterLayoutId: Flow<LayoutId?> = characters

    override val functionLayoutId: Flow<LayoutId?> = functions

    override suspend fun setCharacterLayoutId(id: LayoutId) {
        characters.value = id
    }

    override suspend fun setFunctionLayoutId(id: LayoutId) {
        functions.value = id
    }
}
