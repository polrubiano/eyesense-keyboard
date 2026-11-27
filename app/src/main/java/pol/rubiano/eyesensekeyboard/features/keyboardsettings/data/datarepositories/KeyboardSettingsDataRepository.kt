package pol.rubiano.eyesensekeyboard.features.keyboardsettings.data.datarepositories

import kotlinx.coroutines.flow.Flow
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.data.local.datastore.KeyboardSettingsDataStoreManager
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.repositories.KeyboardSettingsRepository
import javax.inject.Inject

/**
 * The keyboard settings, stored in their DataStore ([local]). It adds no rules of its own: it's
 * the boundary that keeps DataStore inside `data`, so the rest of the app only sees
 * [KeyboardSettingsRepository] and Kotlin types.
 */
class KeyboardSettingsDataRepository @Inject constructor(
    private val local: KeyboardSettingsDataStoreManager,
) : KeyboardSettingsRepository {

    override val characterLayoutId: Flow<LayoutId?> = local.characterLayoutId

    override val functionLayoutId: Flow<LayoutId?> = local.functionLayoutId

    override suspend fun setCharacterLayoutId(id: LayoutId) = local.setCharacterLayoutId(id)

    override suspend fun setFunctionLayoutId(id: LayoutId) = local.setFunctionLayoutId(id)
}
