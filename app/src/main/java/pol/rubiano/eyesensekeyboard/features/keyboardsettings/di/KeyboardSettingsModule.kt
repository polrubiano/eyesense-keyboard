package pol.rubiano.eyesensekeyboard.features.keyboardsettings.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.data.datarepositories.KeyboardSettingsDataRepository
import pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.repositories.KeyboardSettingsRepository
import javax.inject.Singleton

/**
 * One DataStore per file and per process: the delegate guarantees it.
 * The file name is stored on the device: never rename it, or users lose their settings.
 */
private val Context.keyboardSettings: DataStore<Preferences> by preferencesDataStore("keyboard_settings")

/**
 * Tells Hilt how to build the keyboard settings' data layer. Everything is a singleton: one
 * settings file for the whole app.
 *
 * ```
 * KeyboardSettingsRepository ──@Binds──► KeyboardSettingsDataRepository
 *                                             └── KeyboardSettingsDataStoreManager
 *                                                    └── DataStore<Preferences> "keyboard_settings"
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
interface KeyboardSettingsModule {

    @Binds
    @Singleton
    fun bindKeyboardSettingsRepository(repository: KeyboardSettingsDataRepository): KeyboardSettingsRepository

    companion object {

        @Provides
        @Singleton
        fun provideKeyboardSettings(@ApplicationContext context: Context): DataStore<Preferences> =
            context.keyboardSettings
    }
}
