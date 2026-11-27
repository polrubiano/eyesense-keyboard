package pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datastore

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
 * The active layout ids in Preferences DataStore: a small key → value file, made for settings.
 *
 * ```
 * keyboard_settings.preferences_pb
 *   active_character_layout = "user.1"
 *   active_function_layout  = "preset.functions"
 * ```
 *
 * Each id is a separate key, so each [Flow] only emits when its own id changes
 * (`distinctUntilChanged`). Changing the language doesn't make the functions emit.
 *
 * It only stores the two ids. Whether they still point to a layout, and what to use when they
 * don't, is decided by
 * [LayoutDataRepository][pol.rubiano.eyesensekeyboard.features.keyboard.data.datarepositories.LayoutDataRepository].
 * It has no interface because it's the only implementation: tests use it too, writing to a
 * temporary file.
 */
class ActiveLayoutDataStoreManager @Inject constructor(
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
