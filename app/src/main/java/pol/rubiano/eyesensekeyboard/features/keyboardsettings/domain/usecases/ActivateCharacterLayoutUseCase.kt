package pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.usecases

import kotlinx.coroutines.flow.first
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.repositories.LayoutRepository
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.repositories.KeyboardSettingsRepository
import javax.inject.Inject

/**
 * Chooses the language: stores [id] as the user's choice. Only a layout that exists (preset or
 * user) can be chosen. Asking for one that doesn't is a bug in the caller, so it fails right away.
 */
class ActivateCharacterLayoutUseCase @Inject constructor(
    private val layouts: LayoutRepository,
    private val settings: KeyboardSettingsRepository,
) {
    suspend operator fun invoke(id: LayoutId) {
        require(layouts.characterLayouts.first().any { it.id == id }) { "There's no character layout with id ${id.value}" }
        settings.setCharacterLayoutId(id)
    }
}
