package pol.rubiano.eyesensekeyboard.features.keyboardsettings.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pol.rubiano.eyesensekeyboard.R
import pol.rubiano.eyesensekeyboard.app.presentation.theme.EyeSenseKeyboardTheme
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexPanel
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.PanelGeometry
import kotlin.math.sqrt

/** How many setting rows a tab shows: one per notch between the [TAB_SLOTS] tabs. */
const val SETTING_ROWS = TAB_SLOTS - 1

/** Space between the rows, and between a row's point and its notch. */
private val GAP = 4.dp

/**
 * The keyboard settings, drawn in the keyboard's place: the screen gives it the same slot as
 * the hex panel, so the message field above doesn't move when they swap.
 *
 * The geometry comes from the lying-down tabs. Each row is one tab tall and starts half a tab
 * lower, so its centre sits on the junction between two tabs and its point fits that notch:
 * ```
 *  ⬣ ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾       ┐ hexHeight
 *    ‹ Castellano  (●  )        ┘ ← starts at hexHeight / 2
 *  ⬣
 *    ‹ English     (  ○)
 *  ⬣
 *    ‹ Usuario     (  ○)
 *  ⬣ ✕
 *  └──┘ the notch's deepest point is ¾ of a tab's width in
 * ```
 * A tab has the radius of [panel]'s keys, so the eye finds a familiar target size. Lying down,
 * the 4 tabs are only 4·√3·r tall, which always fits in the keyboard's height: they never shrink.
 * The block is centred vertically in the slot.
 *
 * The selected tab is UI state only (which rows are on screen), so it lives here, with
 * `rememberSaveable` so a rotation keeps it. [onToggle] gets the tab and the row.
 */
@Composable
fun KeyboardSettingsPanel(
    panel: HexPanel,
    sections: List<SettingsSection>,
    onToggle: (tab: Int, row: Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val rows = sections[selected].rows

    BoxWithConstraints(modifier, contentAlignment = Alignment.CenterStart) {
        val density = LocalDensity.current
        val geometry = PanelGeometry.fit(panel, constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        val hexHeight = with(density) { (sqrt(3f) * geometry.hexRadius).toDp() }
        val hexWidth = hexHeight * FLAT_HEXAGON_ASPECT

        Box {
            SettingsTabs(
                tabs = sections.map { it.tab },
                selected = selected,
                onTabClick = { selected = it },
                onClose = onClose,
                hexHeight = hexHeight,
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = hexWidth * 0.75f + GAP, top = hexHeight / 2),
                verticalArrangement = Arrangement.spacedBy(GAP),
            ) {
                val rowModifier = Modifier.fillMaxWidth().height(hexHeight - GAP)
                rows.forEachIndexed { index, row ->
                    SettingRow(row, onToggle = { onToggle(selected, index) }, modifier = rowModifier)
                }
                repeat(SETTING_ROWS - rows.size) { Spacer(rowModifier) }
            }
        }
    }
}

/** A tab and the rows it shows. */
data class SettingsSection(val tab: SettingsTab, val rows: List<SettingRowState>) {
    init {
        require(rows.size <= SETTING_ROWS) { "${rows.size} rows don't fit in $SETTING_ROWS" }
    }
}

/**
 * Sample sections, to see the whole panel until each tab is connected: Idioma (to the active
 * layout, next), and two placeholder tabs with three sub-options each. Also used by the preview.
 */
internal val sampleSections = listOf(
    SettingsSection(
        SettingsTab(R.drawable.ic_language, R.string.settings_tab_language),
        listOf(
            SettingRowState("Castellano", checked = true),
            SettingRowState("English", checked = false),
            SettingRowState("Usuario", checked = false, enabled = false),
        ),
    ),
    SettingsSection(
        SettingsTab(R.drawable.ic_speak_text, R.string.settings_tab_sample_2),
        listOf(
            SettingRowState("Opción 2 · 1", checked = false),
            SettingRowState("Opción 2 · 2", checked = true),
            SettingRowState("Opción 2 · 3", checked = false),
        ),
    ),
    SettingsSection(
        SettingsTab(R.drawable.ic_mood, R.string.settings_tab_sample_3),
        listOf(
            SettingRowState("Opción 3 · 1", checked = true),
            SettingRowState("Opción 3 · 2", checked = false),
            SettingRowState("Opción 3 · 3", checked = true),
        ),
    ),
)

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun KeyboardSettingsPanelPreview() {
    EyeSenseKeyboardTheme {
        val ratio = PanelGeometry(Presets.PANEL, hexRadius = 1f).let { it.width / it.height }
        KeyboardSettingsPanel(
            panel = Presets.PANEL,
            sections = sampleSections,
            onToggle = { _, _ -> },
            onClose = {},
            modifier = Modifier.padding(8.dp).fillMaxWidth().aspectRatio(ratio),
        )
    }
}
