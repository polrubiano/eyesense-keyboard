package pol.rubiano.eyesensekeyboard.features.keyboard.presentation.components

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pol.rubiano.eyesensekeyboard.R
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAction
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAssignment
import pol.rubiano.eyesensekeyboard.features.keyboard.presentation.hexgrid.HexSlotScope

/**
 * Draws one key inside its slot. [key] is null for a free slot, which draws nothing at all:
 * neither fill nor outline. That's how an open menu "hides" the rest of the keyboard.
 * What a key shows and how it looks come from its [face]: this function only turns the
 * face into Compose.
 *
 * Glyphs and icons take the same share of the label area, so they look balanced.
 */
@Composable
fun HexSlotScope.KeyView(key: KeyAssignment?) {
    if (key == null) return
    val colors = MaterialTheme.colorScheme
    val face = key.face()
    val (fill, onFill) = when (face.style) {
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
    when (val content = face.content) {
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
    }
}

/**
 * How a key looks: what it shows ([content]) and with which style ([style]).
 *
 * It's decided in **one place**, [face], so [KeyView] only draws what it's told.
 * The style follows the key's **content**, not its shape: if the space bar ends up
 * writing "e" after a swap, it looks like a letter.
 */
data class KeyFace(val content: Content, val style: Style) {

    sealed interface Content {
        /** Text drawn with the font (a letter, a phrase). */
        data class Glyph(val text: String) : Content

        /**
         * A vector silhouette, tinted with the theme colour. [description] is what
         * TalkBack reads, since an icon has no text.
         */
        data class Icon(@DrawableRes val icon: Int, @StringRes val description: Int) : Content
    }

    enum class Style {
        /** Keys that write characters. */
        CHARACTER,

        /** Keys that act: functions, and control-like keys such as the space bar. */
        ACTION,
    }
}

/**
 * The face of a key.
 * - Space → icon + action style. A "␣" glyph sits on the font's baseline and looks low;
 *   the icon is centred by its geometry.
 * - Any other text → uppercase glyph + character style (letters are stored in lowercase).
 * - Function → its icon + action style.
 * - Choice → its option's pictogram (an emoji) + action style; "none" → [NONE_PICTOGRAM].
 */
fun KeyAssignment.face(): KeyFace = when (this) {
    is KeyAssignment.Text ->
        if (value == " ") {
            KeyFace(KeyFace.Content.Icon(R.drawable.ic_space_bar, R.string.key_space), KeyFace.Style.ACTION)
        } else {
            KeyFace(KeyFace.Content.Glyph(value.uppercase()), KeyFace.Style.CHARACTER)
        }
    is KeyAssignment.Function ->
        KeyFace(KeyFace.Content.Icon(action.icon(), action.description()), KeyFace.Style.ACTION)
    // Every option brings its own pictogram; "none" isn't an option, so the UI gives it one.
    is KeyAssignment.Choice ->
        KeyFace(KeyFace.Content.Glyph(option?.pictogram ?: NONE_PICTOGRAM), KeyFace.Style.ACTION)
}

/** The centre of every menu: "none", which removes the menu's value. */
private const val NONE_PICTOGRAM = "🚫"

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

/** What TalkBack reads for a function key. */
@StringRes
fun KeyAction.description(): Int = when (this) {
    KeyAction.DeleteCharacter -> R.string.key_delete_character
    KeyAction.ClearText -> R.string.key_clear_text
    KeyAction.Speak -> R.string.key_speak
    KeyAction.SelectEmotion -> R.string.key_select_emotion
}
