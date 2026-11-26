package pol.rubiano.eyesensekeyboard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import pol.rubiano.eyesensekeyboard.app.presentation.theme.EyeSenseKeyboardTheme
import pol.rubiano.eyesensekeyboard.features.keyboard.presentation.screens.KeyboardScreen

/** `@AndroidEntryPoint`: lets Hilt hand out dependencies here, e.g. the screen's `hiltViewModel()`. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EyeSenseKeyboardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    KeyboardScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}
