package pol.rubiano.eyesensekeyboard.keyboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pol.rubiano.eyesensekeyboard.app.presentation.theme.EyeSenseKeyboardTheme
import pol.rubiano.eyesensekeyboard.hexgrid.presentation.HexPanelLayout
import pol.rubiano.eyesensekeyboard.hexgrid.presentation.HexSlotScope
import pol.rubiano.eyesensekeyboard.keyboard.domain.CharacterLayout
import pol.rubiano.eyesensekeyboard.keyboard.domain.KeyAction
import pol.rubiano.eyesensekeyboard.keyboard.domain.KeyAssignment
import pol.rubiano.eyesensekeyboard.keyboard.domain.Keyboard
import pol.rubiano.eyesensekeyboard.keyboard.domain.Presets

/*
 * Preview of the layouts. The labels and symbols are provisional: the real key, with icons
 * and semantics (KeyView), will come in the "functional keyboard" feature.
 */

/** Provisional label for each key's content. */
private fun KeyAssignment.previewLabel(): String = when (this) {
    is KeyAssignment.Text -> if (value == " ") "␣" else value.uppercase()
    is KeyAssignment.Function -> when (action) {
        KeyAction.DeleteCharacter -> "⌫"
        KeyAction.ClearText -> "✕"
        KeyAction.Speak -> "🔊"
        KeyAction.SelectEmotion -> "☺"
    }
}

@Composable
private fun HexSlotScope.PreviewKey(key: KeyAssignment?) {
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
            text = key.previewLabel(),
            modifier = Modifier.alignToSlotCenter(),
            color = if (key is KeyAssignment.Function) colors.onPrimaryContainer else colors.onSurfaceVariant,
            fontSize = with(LocalDensity.current) { (labelAreaSize * 0.45f).toSp() },
        )
    }
}

@Composable
private fun LayoutPreview(characters: CharacterLayout) {
    val keyboard = Keyboard(Presets.PANEL, characters, Presets.DEFAULT_FUNCTIONS)
    EyeSenseKeyboardTheme(darkTheme = true, dynamicColor = false) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1C1E)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "${characters.name} + ${keyboard.functions.name}",
                color = Color.White,
                modifier = Modifier.padding(12.dp),
            )
            HexPanelLayout(panel = keyboard.panel) { slot ->
                PreviewKey(keyboard.keyAt(slot))
            }
        }
    }
}

@Preview(name = "Castellano + funciones", widthDp = 411, heightDp = 360)
@Composable
private fun SpanishLayoutPreview() = LayoutPreview(Presets.SPANISH)

@Preview(name = "English + funciones", widthDp = 411, heightDp = 360)
@Composable
private fun EnglishLayoutPreview() = LayoutPreview(Presets.ENGLISH)
