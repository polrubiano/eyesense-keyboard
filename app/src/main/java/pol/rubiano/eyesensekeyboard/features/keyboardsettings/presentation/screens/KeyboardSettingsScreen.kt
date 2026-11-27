package pol.rubiano.eyesensekeyboard.features.keyboardsettings.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pol.rubiano.eyesensekeyboard.R
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexPanel
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.presentation.components.KeyboardSettingsPanel
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.presentation.components.LanguageTab
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.presentation.components.SettingRowState
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.presentation.components.SettingsSection
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.presentation.components.placeholderSections
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.presentation.viewmodels.KeyboardSettingsViewModel

/**
 * The keyboard settings' entry point: it takes the keyboard's slot, not the whole screen, so
 * the host only gives it the [panel] (for the key size), a slot ([modifier]) and [onClose].
 *
 * It's the only place that knows the [KeyboardSettingsViewModel]: it turns its state into the
 * panel's rows, and the panel's (tab, row) taps back into actions. Tab 0 is Idioma; the other
 * tabs are placeholders and do nothing yet.
 */
@Composable
fun KeyboardSettingsScreen(
    panel: HexPanel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KeyboardSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val userLabel = stringResource(R.string.settings_language_user)

    val languages = SettingsSection(
        LanguageTab,
        state.languages.map { SettingRowState(it.name ?: userLabel, checked = it.active, enabled = it.enabled) },
    )

    KeyboardSettingsPanel(
        panel = panel,
        sections = listOf(languages) + placeholderSections,
        onToggle = { tab, row ->
            if (tab == LANGUAGE_TAB) state.languages[row].id?.let(viewModel::onLanguageToggle)
        },
        onClose = onClose,
        modifier = modifier,
    )
}

private const val LANGUAGE_TAB = 0
