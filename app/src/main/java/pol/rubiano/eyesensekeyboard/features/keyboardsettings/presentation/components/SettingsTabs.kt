package pol.rubiano.eyesensekeyboard.features.keyboardsettings.presentation.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pol.rubiano.eyesensekeyboard.R

/** A settings tab: the icon its hexagon shows and what TalkBack reads. */
data class SettingsTab(@DrawableRes val icon: Int, @StringRes val description: Int)

/**
 * How many hexagons the column holds, close included. Fixed on purpose: free slots keep their
 * space, so adding a tab later doesn't move anything the eye has already learnt. 4 hexagons
 * leave 3 notches between them: one per setting row ([SETTING_ROWS]).
 */
const val TAB_SLOTS = 4

/**
 * The column of lying-down hexagon tabs, stacked with no gap so the notches between them are
 * exact:
 * ```
 * ⬣ tabs[0]     ← selected: filled + border (never colour alone)
 * ⬣ tabs[1]
 * ·  (free)     ← keeps its space, draws nothing
 * ⬣ ✕           ← always the last slot: closes the settings
 * ```
 * [hexHeight] is each hexagon's box height (√3·r for the keyboard key's radius r).
 */
@Composable
fun SettingsTabs(
    tabs: List<SettingsTab>,
    selected: Int,
    onTabClick: (Int) -> Unit,
    onClose: () -> Unit,
    hexHeight: Dp,
    modifier: Modifier = Modifier,
) {
    require(tabs.size < TAB_SLOTS) { "${tabs.size} tabs + close don't fit in $TAB_SLOTS slots" }
    val slot = Modifier.size(width = hexHeight * FLAT_HEXAGON_ASPECT, height = hexHeight)

    Column(modifier) {
        tabs.forEachIndexed { index, tab ->
            TabHexagon(tab.icon, stringResource(tab.description), index == selected, { onTabClick(index) }, slot)
        }
        repeat(TAB_SLOTS - 1 - tabs.size) { Spacer(slot) }
        TabHexagon(R.drawable.ic_close, stringResource(R.string.settings_close), false, onClose, slot)
    }
}

@Composable
private fun TabHexagon(
    @DrawableRes icon: Int,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier
            // The slot is exact; the drawn hexagon shrinks a little so neighbours don't touch.
            .padding(2.dp)
            .clip(FlatHexagonShape)
            .background(if (selected) colors.surface else colors.surfaceVariant)
            .then(if (selected) Modifier.border(2.dp, colors.primary, FlatHexagonShape) else Modifier)
            .semantics { this.selected = selected }
            .clickable(role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = description,
            tint = if (selected) colors.primary else colors.onSurfaceVariant,
        )
    }
}
