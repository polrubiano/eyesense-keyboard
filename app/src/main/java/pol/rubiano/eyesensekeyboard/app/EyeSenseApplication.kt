package pol.rubiano.eyesensekeyboard.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The app's entry point for Hilt: `@HiltAndroidApp` generates the container that lives as long
 * as the app does. Singletons (the database, the repositories) are created there, and every
 * `@AndroidEntryPoint` and `@HiltViewModel` gets its dependencies from it.
 */
@HiltAndroidApp
class EyeSenseApplication : Application()
