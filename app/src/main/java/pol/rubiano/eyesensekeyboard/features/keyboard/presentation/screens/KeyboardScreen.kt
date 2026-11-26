package pol.rubiano.eyesensekeyboard.features.keyboard.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.SpokenMessage
import pol.rubiano.eyesensekeyboard.features.keyboard.presentation.components.KeyView
import pol.rubiano.eyesensekeyboard.features.keyboard.presentation.components.MessageField
import pol.rubiano.eyesensekeyboard.features.keyboard.presentation.hexgrid.HexPanelLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.presentation.viewmodels.KeyboardViewModel

/**
 * The writing screen. Everything here will float over the camera background later on.
 *
 * Column
 * ├── Box weight(1f), content at the bottom  ← free space (future predictions)
 * │     └── Column
 * │           ├── SpokenLog                   ← temporary: what Speak would say, scrollable
 * │           └── MessageField                ← grows upwards as text is added
 * └── HexPanelLayout                          ← never shrinks
 *
 * The field lives inside the flexible space, so when it grows it takes room from the free
 * space, never from the keyboard.
 *
 * The emotion reaches the screen twice, in two forms: its pictogram in the field (for the
 * person) and its id inside the payload (for the systems that will receive it).
 */
@Composable
fun KeyboardScreen(
    modifier: Modifier = Modifier,
    viewModel: KeyboardViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val keyboard = state.keyboard

    Column(modifier.fillMaxSize()) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
            ) {
                // fill = false: the log is as tall as its messages, up to whatever the field leaves free.
                SpokenLog(state.spoken, Modifier.weight(1f, fill = false))
                MessageField(state.text, pictogram = state.emotion?.pictogram)
            }
        }

        HexPanelLayout(
            panel = keyboard.panel,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(8.dp),
            onSlotClick = viewModel::onSlotClick,
        ) { slot ->
            KeyView(keyboard.keyAt(slot))
        }
    }
}

/**
 * Workaround until Speak has a voice: every message said, as its payload (`[happy][hola]`).
 *
 * `reverseLayout` anchors the list to the bottom, so the newest message sits right above the
 * field and the older ones are pushed up. Swipe down to read them.
 */
@Composable
private fun SpokenLog(messages: List<SpokenMessage>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(messages.asReversed()) { message ->
            Text(
                text = message.payload,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
