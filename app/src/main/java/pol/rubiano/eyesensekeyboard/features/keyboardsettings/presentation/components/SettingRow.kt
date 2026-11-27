package pol.rubiano.eyesensekeyboard.features.keyboardsettings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** What a setting row shows. */
data class SettingRowState(val label: String, val checked: Boolean, val enabled: Boolean = true)

/**
 * One setting: its description and a switch, inside a shape whose point fits the notch
 * between two tabs.
 * ```
 *  ╱‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾╲
 * ‹  Castellano               (●  )  │
 *  ╲_______________________________╱
 * ```
 * **Only the switch is active.** The rest of the row is there to be read: with gaze, reading a
 * row means looking at it, and that must never change anything. TalkBack reads the switch with
 * the row's label.
 */
@Composable
fun SettingRow(state: SettingRowState, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier
            .alpha(if (state.enabled) 1f else DISABLED_ALPHA)
            .clip(PointedRowShape)
            .background(colors.surface)
            .border(1.dp, colors.outline, PointedRowShape)
            // The start padding clears the point of the shape.
            .padding(start = 32.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = state.label,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = state.checked,
            onCheckedChange = { onToggle() },
            enabled = state.enabled,
            modifier = Modifier.semantics { contentDescription = state.label },
        )
    }
}

/** Material's opacity for disabled content. */
private const val DISABLED_ALPHA = 0.38f

/**
 * A rectangle whose left side is a point. Its sides follow the flat hexagons' edges (60° to
 * the vertical), so the point fills the notch between two tabs exactly:
 * the point is `height × tan(30°) / 2 ≈ height × 0.29` deep.
 */
private val PointedRowShape = GenericShape { size, _ ->
    val point = size.height * 0.29f
    moveTo(point, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)
    lineTo(point, size.height)
    lineTo(0f, size.height / 2f)
    close()
}
