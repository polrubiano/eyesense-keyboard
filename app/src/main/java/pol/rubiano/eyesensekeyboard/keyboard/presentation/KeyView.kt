package pol.rubiano.eyesensekeyboard.keyboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pol.rubiano.eyesensekeyboard.hexgrid.presentation.HexSlotScope
import pol.rubiano.eyesensekeyboard.keyboard.domain.KeyAssignment

/**
 * Draws one key inside its slot. [key] is null for a free slot, which is drawn as an
 * outline only. What it shows and how it looks come from its [face]: this function only
 * turns the face into Compose.
 *
 * Glyphs and icons take the same share of the label area, so they look balanced.
 */
@Composable
fun HexSlotScope.KeyView(key: KeyAssignment?) {
    val colors = MaterialTheme.colorScheme
    val face = key?.face()
    val (fill, onFill) = when (face?.style) {
        null -> Color.Transparent to Color.Transparent
        KeyFace.Style.CHARACTER -> colors.surfaceVariant to colors.onSurfaceVariant
        KeyFace.Style.ACTION -> colors.primaryContainer to colors.onPrimaryContainer
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(fill, shape)
            .border(1.dp, colors.outline, shape)
    )
    val contentSize = labelAreaSize * 0.45f
    when (val content = face?.content) {
        is KeyFace.Content.Glyph -> Text(
            text = content.text,
            modifier = Modifier.alignToSlotCenter(),
            color = onFill,
            fontSize = with(LocalDensity.current) { contentSize.toSp() },
        )
        is KeyFace.Content.Icon -> Icon(
            painter = painterResource(content.icon),
            contentDescription = stringResource(content.description),
            modifier = Modifier
                .alignToSlotCenter()
                .size(contentSize),
            tint = onFill,
        )
        null -> Unit
    }
}
