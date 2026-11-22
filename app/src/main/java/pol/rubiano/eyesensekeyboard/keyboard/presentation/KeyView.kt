package pol.rubiano.eyesensekeyboard.keyboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import pol.rubiano.eyesensekeyboard.hexgrid.presentation.HexSlotScope
import pol.rubiano.eyesensekeyboard.keyboard.domain.KeyAction
import pol.rubiano.eyesensekeyboard.keyboard.domain.KeyAssignment

/**
 * What a key shows. Letters are stored in lowercase and shown in uppercase.
 * The function symbols are provisional until the functions get their real behaviour.
 */
fun KeyAssignment.label(): String = when (this) {
    is KeyAssignment.Text -> if (value == " ") "␣" else value.uppercase()
    is KeyAssignment.Function -> when (action) {
        KeyAction.DeleteCharacter -> "⌫"
        KeyAction.ClearText -> "✕"
        KeyAction.Speak -> "🔊"
        KeyAction.SelectEmotion -> "☺"
    }
}

/**
 * Draws one key inside its slot (hexagon or triangle). [key] is null for a free slot,
 * which is drawn as an outline only.
 */
@Composable
fun HexSlotScope.KeyView(key: KeyAssignment?) {
    val colors = MaterialTheme.colorScheme
    val fill = when (key) {
        null -> Color.Transparent
        is KeyAssignment.Function -> colors.primaryContainer
        is KeyAssignment.Text -> colors.surfaceVariant
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(fill, shape)
            .border(1.dp, colors.outline, shape)
    )
    if (key != null) {
        Text(
            text = key.label(),
            modifier = Modifier.alignToSlotCenter(),
            color = if (key is KeyAssignment.Function) colors.onPrimaryContainer else colors.onSurfaceVariant,
            fontSize = with(LocalDensity.current) { (labelAreaSize * 0.45f).toSp() },
        )
    }
}
