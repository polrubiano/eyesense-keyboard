package pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.repositories.LayoutRepository
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.models.ActiveLayouts
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.repositories.KeyboardSettingsRepository
import javax.inject.Inject

/**
 * The layouts in use: the user's choice ([settings]) looked up among the layouts that exist
 * ([layouts]). There's always one of each: if nothing was chosen yet, or the chosen layout was
 * deleted, the preset is used.
 *
 * ```
 * layouts ─────────┐
 *                  ├─ combine ─► the layout with the chosen id, or the preset
 * chosen ids ──────┘
 * ```
 * Because it's derived from the list, deleting the chosen layout needs no extra code: the list
 * changes, and the keyboard falls back to the preset on its own. The choice stays stored, so
 * nothing is lost if a layout with that id comes back.
 */
class GetActiveLayoutsUseCase @Inject constructor(
    private val layouts: LayoutRepository,
    private val settings: KeyboardSettingsRepository,
) {
    operator fun invoke(): Flow<ActiveLayouts> {
        val characters = combine(layouts.characterLayouts, settings.characterLayoutId) { all, id ->
            all.firstOrNull { it.id == id } ?: Presets.SPANISH
        }
        val functions = combine(layouts.functionLayouts, settings.functionLayoutId) { all, id ->
            all.firstOrNull { it.id == id } ?: Presets.DEFAULT_FUNCTIONS
        }
        return combine(characters, functions, ::ActiveLayouts).distinctUntilChanged()
    }
}
