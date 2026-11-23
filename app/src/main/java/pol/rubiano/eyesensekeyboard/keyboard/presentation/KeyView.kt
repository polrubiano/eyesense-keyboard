package pol.rubiano.eyesensekeyboard.keyboard.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import pol.rubiano.eyesensekeyboard.R
import pol.rubiano.eyesensekeyboard.hexgrid.presentation.HexSlotScope
import pol.rubiano.eyesensekeyboard.keyboard.domain.KeyAction
import pol.rubiano.eyesensekeyboard.keyboard.domain.KeyAssignment

/** What a text key shows. Letters are stored in lowercase and shown in uppercase. */
fun KeyAssignment.Text.label(): String = if (value == " ") "␣" else value.uppercase()

/**
 * The icon a function key shows. It's a silhouette: its colour comes from the theme
 * (the `tint` in [KeyView]), so it follows any future style change.
 */
@DrawableRes
fun KeyAction.icon(): Int = when (this) {
    KeyAction.DeleteCharacter -> R.drawable.ic_backspace
    KeyAction.ClearText -> R.drawable.ic_clear_all
    KeyAction.Speak -> R.drawable.ic_speak_text
    KeyAction.SelectEmotion -> R.drawable.ic_mood
}

/** What TalkBack reads for a function key, since the icon has no text. */
@StringRes
fun KeyAction.description(): Int = when (this) {
    KeyAction.DeleteCharacter -> R.string.key_delete_character
    KeyAction.ClearText -> R.string.key_clear_text
    KeyAction.Speak -> R.string.key_speak
    KeyAction.SelectEmotion -> R.string.key_select_emotion
}

/**
 * Draws one key inside its slot (hexagon or corner key). [key] is null for a free slot,
 * which is drawn as an outline only.
 *
 * Text keys show their [label]; function keys show their [icon], at the same relative size.
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
    val contentSize = labelAreaSize * 0.45f
    when (key) {
        is KeyAssignment.Text -> Text(
            text = key.label(),
            modifier = Modifier.alignToSlotCenter(),
            color = colors.onSurfaceVariant,
            fontSize = with(LocalDensity.current) { contentSize.toSp() },
        )
        is KeyAssignment.Function -> Icon(
            painter = painterResource(key.action.icon()),
            contentDescription = stringResource(key.action.description()),
            modifier = Modifier
                .alignToSlotCenter()
                .size(contentSize),
            tint = colors.onPrimaryContainer,
        )
        null -> Unit
    }
}
