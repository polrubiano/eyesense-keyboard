package pol.rubiano.eyesensekeyboard.app

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import pol.rubiano.eyesensekeyboard.app.presentation.theme.EyeSenseKeyboardTheme
import pol.rubiano.eyesensekeyboard.features.keyboard.presentation.screens.KeyboardScreen
import pol.rubiano.eyesensekeyboard.features.keyboard.presentation.viewmodels.KeyboardViewModel

/** `@AndroidEntryPoint`: lets Hilt hand out dependencies here, e.g. the screen's `hiltViewModel()`. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * The same instance the screen gets with `hiltViewModel()`: both ask the Activity for it.
     * Here it's only used to know when the splash can go.
     */
    private val keyboardViewModel: KeyboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        keepSplashUntilReady()
        setContent {
            EyeSenseKeyboardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    KeyboardScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }

    /**
     * Since Android 12 the system shows a splash (the app icon) on launch, and it stays until
     * the first frame is drawn. Holding that first frame back keeps the splash on screen until
     * the keyboard knows the user's chosen language, so it never shows another one first.
     * It's Android's documented pattern, and needs no library because `minSdk` is 33.
     *
     * The read takes a few milliseconds; if storage fails, [KeyboardViewModel.isReady] still
     * turns true and the presets keyboard shows.
     */
    private fun keepSplashUntilReady() {
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (!keyboardViewModel.isReady.value) return false
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            },
        )
    }
}
