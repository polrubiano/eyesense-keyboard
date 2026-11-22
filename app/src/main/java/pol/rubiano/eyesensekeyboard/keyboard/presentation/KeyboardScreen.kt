package pol.rubiano.eyesensekeyboard.keyboard.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pol.rubiano.eyesensekeyboard.hexgrid.presentation.HexPanelLayout

/**
 * The writing screen. Everything here will float over the camera background later on.
 *
 * Column
 * ├── Box weight(1f), content at the bottom  ← free space (future predictions)
 * │     └── MessageField                      ← grows upwards as text is added
 * └── HexPanelLayout                          ← never shrinks
 *
 * The field lives inside the flexible space, so when it grows it takes room from the free
 * space, never from the keyboard.
 */
@Composable
fun KeyboardScreen(
    modifier: Modifier = Modifier,
    viewModel: KeyboardViewModel = viewModel(),
) {
    val text by viewModel.text.collectAsStateWithLifecycle()
    val keyboard = viewModel.keyboard

    Column(modifier.fillMaxSize()) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            MessageField(text)
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
