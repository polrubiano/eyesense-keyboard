package pol.rubiano.eyesensekeyboard.hexgrid.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pol.rubiano.eyesensekeyboard.app.presentation.theme.EyeSenseKeyboardTheme
import pol.rubiano.eyesensekeyboard.hexgrid.domain.CornerPosition
import pol.rubiano.eyesensekeyboard.hexgrid.domain.HexPanel
import pol.rubiano.eyesensekeyboard.hexgrid.domain.PanelSlot

/*
 * Preview data only. Assigning keys to slots will be its own step later on;
 * here it only helps to see the panel with content.
 */

private val previewPanel = HexPanel(rowsAroundCenter = 2, rings = 3)

/** Approximate frequency order in Spanish + space. The free slot shows up empty. */
private val sampleLabels = listOf(
    "E", "A", "O", "S", "R", "N", "I", "D", "L", "C", "T", "U", "M", "P",
    "B", "G", "V", "Y", "Q", "H", "F", "Z", "J", "Ñ", "X", "K", "W", "␣",
)

/** The panel's slots are ordered by closeness to the centre, so pairing the two lists = assigning by frequency. */
private val cellLabels = previewPanel.cells.zip(sampleLabels).toMap()

private val cornerLabels = mapOf(
    CornerPosition.TOP_LEFT to "F1",
    CornerPosition.TOP_RIGHT to "⌫",
    CornerPosition.BOTTOM_LEFT to "F2",
    CornerPosition.BOTTOM_RIGHT to "F3",
)

private fun labelFor(slot: PanelSlot): String? = when (slot) {
    is PanelSlot.Cell -> cellLabels[slot.coord]
    is PanelSlot.Corner -> cornerLabels[slot.position]
}

@Composable
private fun HexSlotScope.PreviewKey(label: String?, isCorner: Boolean, isSelected: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    val fill = when {
        isSelected -> colors.tertiary
        isCorner -> colors.primaryContainer
        label == null -> Color.Transparent
        else -> colors.surfaceVariant
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(fill, shape)
            .border(1.dp, colors.outline, shape)
    )
    if (label != null) {
        Text(
            text = label,
            modifier = Modifier.alignToSlotCenter(),
            color = when {
                isSelected -> colors.onTertiary
                isCorner -> colors.onPrimaryContainer
                else -> colors.onSurfaceVariant
            },
            fontSize = with(LocalDensity.current) { (labelAreaSize * 0.45f).toSp() },
        )
    }
}

@Composable
private fun KeyboardPreviewContent() {
    EyeSenseKeyboardTheme(darkTheme = true, dynamicColor = false) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1C1E)),
            contentAlignment = Alignment.Center,
        ) {
            HexPanelLayout(panel = previewPanel, gap = 4.dp) { slot ->
                PreviewKey(label = labelFor(slot), isCorner = slot is PanelSlot.Corner)
            }
        }
    }
}

@Preview(name = "Portrait - bottom 35%", widthDp = 411, heightDp = 320)
@Composable
private fun HexPanelPortraitPreview() = KeyboardPreviewContent()

@Preview(name = "Small phone", widthDp = 320, heightDp = 260)
@Composable
private fun HexPanelSmallPreview() = KeyboardPreviewContent()

@Preview(name = "Landscape - height governs", widthDp = 891, heightDp = 200)
@Composable
private fun HexPanelLandscapePreview() = KeyboardPreviewContent()

/**
 * Turn on "Interactive Mode" in Android Studio and tap: the slot under your finger lights
 * up and its name shows at the top. Good for checking the edges, the triangles and the nooks.
 */
@Preview(name = "Interactive - try the touch", widthDp = 411, heightDp = 380)
@Composable
private fun HexPanelInteractivePreview() {
    var selected by remember { mutableStateOf<PanelSlot?>(null) }

    EyeSenseKeyboardTheme(darkTheme = true, dynamicColor = false) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1C1E)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = selected?.let { "${labelFor(it) ?: "(free)"} · $it" } ?: "Tap a key",
                color = Color.White,
                modifier = Modifier.padding(16.dp),
            )
            HexPanelLayout(
                panel = previewPanel,
                onSlotClick = { selected = it },
            ) { slot ->
                PreviewKey(
                    label = labelFor(slot),
                    isCorner = slot is PanelSlot.Corner,
                    isSelected = slot == selected,
                )
            }
        }
    }
}
