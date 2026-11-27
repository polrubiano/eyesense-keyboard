package pol.rubiano.eyesensekeyboard.app

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import pol.rubiano.eyesensekeyboard.features.keyboard.data.datarepositories.LayoutDataRepository
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datasources.LayoutDbLocalDataSource
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.db.KeyboardDatabase
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.repositories.LayoutRepository
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.data.datarepositories.KeyboardSettingsDataRepository
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.data.local.datastore.KeyboardSettingsDataStoreManager
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.repositories.KeyboardSettingsRepository
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.usecases.ActivateCharacterLayoutUseCase
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.usecases.ActivateFunctionLayoutUseCase
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.usecases.GetActiveLayoutsUseCase
import java.io.File

/**
 * The real storage, built by hand without Hilt, empty for every test: the layouts in Room (in
 * memory) and the keyboard settings in DataStore (a temporary file). The test class must run
 * with `RobolectricTestRunner`, because Room needs an Android context.
 *
 * ```
 * @get:Rule val storage = KeyboardStorageRule()
 * storage.layouts.save(…); storage.activateCharacterLayout(…)
 * ```
 * It closes everything after the test's own `@After`, so a ViewModel can stop there first.
 */
class KeyboardStorageRule : TestWatcher() {

    private val folder = TemporaryFolder()
    private lateinit var db: KeyboardDatabase
    private lateinit var scope: CoroutineScope

    val layouts: LayoutRepository by lazy { LayoutDataRepository(LayoutDbLocalDataSource(db.layoutDao())) }

    val settings: KeyboardSettingsRepository by lazy {
        KeyboardSettingsDataRepository(
            KeyboardSettingsDataStoreManager(
                PreferenceDataStoreFactory.create(scope = scope, produceFile = { File(folder.root, "settings.preferences_pb") }),
            ),
        )
    }

    val getActiveLayouts get() = GetActiveLayoutsUseCase(layouts, settings)
    val activateCharacterLayout get() = ActivateCharacterLayoutUseCase(layouts, settings)
    val activateFunctionLayout get() = ActivateFunctionLayoutUseCase(layouts, settings)

    override fun starting(description: Description) {
        folder.create()
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), KeyboardDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    override fun finished(description: Description) {
        scope.cancel()
        db.close()
        folder.delete()
    }
}
