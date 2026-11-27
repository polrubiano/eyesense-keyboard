package pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.usecases

import kotlinx.coroutines.flow.first
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.repositories.LayoutRepository
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.repositories.KeyboardSettingsRepository
import javax.inject.Inject

/**
 * Chooses the corner functions: stores [id] as the user's choice. Same rule as
 * [ActivateCharacterLayoutUseCase]: only an existing layout can be chosen. No screen uses it
 * yet; it's kept so the two choices stay symmetric.
 */
class ActivateFunctionLayoutUseCase @Inject constructor(
    private val layouts: LayoutRepository,
    private val settings: KeyboardSettingsRepository,
) {
    suspend operator fun invoke(id: LayoutId) {
        require(layouts.functionLayouts.first().any { it.id == id }) { "There's no function layout with id ${id.value}" }
        settings.setFunctionLayoutId(id)
    }
}
