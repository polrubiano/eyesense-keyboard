package pol.rubiano.eyesensekeyboard.features.keyboard.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import pol.rubiano.eyesensekeyboard.R
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

private const val CURSOR_ID = "cursor"

/**
 * Looks like a text field, but it's read-only: the text only changes through the in-app keyboard,
 * so Android's own keyboard never shows up.
 *
 * - It starts one line tall and grows with the text, with no limit of its own: a person using an
 *   eye tracker must be able to see everything without scrolling. The scroll is only a safety
 *   net in case the text ever fills the whole space it's given.
 * - Block ("insert") cursor blinking at the end. Being part of the text, it wraps to the next
 *   line together with it, and it makes a trailing space visible.
 * - No hint when empty: the blinking cursor alone shows where the text will go.
 * - [pictogram] (the message's emotion, if any) sits before the text, outside its flow: the
 *   text wraps beside it, and it stays visible while the field is empty.
 * - A clear button ("x") at the end calls [onClear]. It only shows while there's text, because an
 *   empty field has nothing to clear.
 *
 * ```
 * ┌──────────────────────────────────────┐
 * │ 😊  hola que tal▮               (✕)  │
 * └──────────────────────────────────────┘
 * ```
 */
@Composable
fun MessageField(
    text: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    pictogram: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    val scroll = rememberScrollState()

    // Keeps the end of the text (where the cursor is) in view if the safety scroll ever kicks in.
    LaunchedEffect(text) { scroll.animateScrollTo(scroll.maxValue) }

    val content = buildAnnotatedString {
        append(text)
        appendInlineContent(CURSOR_ID)
    }

    Box(
        modifier
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(1.dp, colors.outline, shape)
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (pictogram != null) {
                Text(text = pictogram, style = MaterialTheme.typography.headlineSmall)
            }
            Text(
                text = content,
                inlineContent = mapOf(CURSOR_ID to blockCursor()),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                // weight: the text takes what's left, so the button stays pinned to the end.
                modifier = Modifier.weight(1f),
            )
            if (text.isNotEmpty()) {
                ClearButton(onClear)
            }
        }
    }
}

/**
 * Small on purpose: a 32 dp circle is about one line of `headlineSmall`, so showing it doesn't
 * make the field taller. `IconButton` was discarded because it reserves 48 dp.
 *
 * It sits at the top of the row (the Row's default alignment), so it stays next to the first
 * line instead of moving around while the text grows.
 */
@Composable
private fun ClearButton(onClick: () -> Unit) {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = stringResource(R.string.key_clear_text),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

/**
 * The cursor: a block about as wide as a letter, inserted into the text as if it were one more
 * character. It blinks on/off (no fade), like a classic terminal.
 *
 * Primary at 40% opacity: solid primary was too heavy next to the text.
 */
@Composable
private fun blockCursor(): InlineTextContent {
    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    val blink by rememberInfiniteTransition(label = "cursor").animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                1f at 0
                1f at 499
                0f at 500
                0f at 999
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "blink",
    )
    return InlineTextContent(
        Placeholder(width = 0.55.em, height = 1.1.em, placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .alpha(blink)
                .background(color)
        )
    }
}
