package pol.rubiano.eyesensekeyboard.features.keyboardsettings.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import java.io.IOException
import javax.inject.Inject

/**
 * The keyboard settings, in their own Preferences DataStore: a small key → value file, made for
 * settings. **Every keyboard setting lives here**; a new setting = a new key + its Flow and setter.
 *
 * ```
 * keyboard_settings.preferences_pb
 *   active_character_layout = "user.1"            ← the language the user chose
 *   active_function_layout  = "preset.functions"  ← the corner functions the user chose
 * ```
 *
 * Each setting is a separate key, so each [Flow] only emits when its own value changes
 * (`distinctUntilChanged`). Changing the language doesn't make the functions emit.
 *
 * It only stores the choices. Whether a chosen layout still exists, and what to use when it
 * doesn't, is decided by the use cases in `keyboardsettings/domain/usecases`.
 */
class KeyboardSettingsDataStoreManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    /**
     * If the file can't be read, it behaves as if nothing was stored: the keyboard falls back
     * to the presets instead of crashing. Any other error is a bug, so it's rethrown.
     */
    private val preferences: Flow<Preferences> = dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    /** Null until a character layout has been activated. */
    val characterLayoutId: Flow<LayoutId?> = idAt(CHARACTER_LAYOUT)

    /** Null until a function layout has been activated. */
    val functionLayoutId: Flow<LayoutId?> = idAt(FUNCTION_LAYOUT)

    suspend fun setCharacterLayoutId(id: LayoutId) {
        dataStore.edit { it[CHARACTER_LAYOUT] = id.value }
    }

    suspend fun setFunctionLayoutId(id: LayoutId) {
        dataStore.edit { it[FUNCTION_LAYOUT] = id.value }
    }

    private fun idAt(key: Preferences.Key<String>): Flow<LayoutId?> =
        preferences.map { it[key]?.let(::LayoutId) }.distinctUntilChanged()

    private companion object {
        // Stored on the device: never rename them, or users lose their choice.
        val CHARACTER_LAYOUT = stringPreferencesKey("active_character_layout")
        val FUNCTION_LAYOUT = stringPreferencesKey("active_function_layout")
    }
}
