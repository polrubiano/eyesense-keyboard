package pol.rubiano.eyesensekeyboard.features.keyboardsettings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import pol.rubiano.eyesensekeyboard.R
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexPanel
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.PanelGeometry
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.Point
import pol.rubiano.eyesensekeyboard.features.keyboard.presentation.hexgrid.PolygonShape

/**
 * The keyboard settings, drawn in the keyboard's place: the screen gives it the same slot as
 * the hex panel, so the message field above doesn't move when they swap.
 *
 * First version: only the close hexagon, at the bottom of what will be the tab column
 * (the last tab is always ✕). The tabs and the setting rows come next.
 *
 * The hexagon is as big as one of the keyboard's ([panel]'s), so the eye finds a familiar
 * target size.
 */
@Composable
fun KeyboardSettingsPanel(
    panel: HexPanel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val geometry = PanelGeometry.fit(panel, constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        val hexWidth = with(density) { geometry.hexWidth.toDp() }
        val hexHeight = with(density) { (2 * geometry.hexRadius).toDp() }

        CloseHexagon(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(width = hexWidth, height = hexHeight),
        )
    }
}

@Composable
private fun CloseHexagon(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(HexagonShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = stringResource(R.string.settings_close),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * A pointy-top hexagon filling its box, like the keyboard's: the box must be √3 wide by 2 tall.
 * ```
 *        (0.5, 0)
 *   (0, .25)    (1, .25)
 *   (0, .75)    (1, .75)
 *        (0.5, 1)
 * ```
 */
private val HexagonShape = PolygonShape(
    listOf(
        Point(0.5f, 0f), Point(1f, 0.25f), Point(1f, 0.75f),
        Point(0.5f, 1f), Point(0f, 0.75f), Point(0f, 0.25f),
    ),
)
