package pol.rubiano.eyesensekeyboard.keyboard.presentation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
 */
@Composable
fun MessageField(text: String, modifier: Modifier = Modifier) {
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
        Text(
            text = content,
            inlineContent = mapOf(CURSOR_ID to blockCursor()),
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onSurface,
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
