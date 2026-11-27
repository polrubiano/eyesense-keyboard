package pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.usecases

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pol.rubiano.eyesensekeyboard.app.KeyboardStorageRule
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets

@RunWith(RobolectricTestRunner::class)
class ActivateCharacterLayoutUseCaseTest {

    @get:Rule
    val storage = KeyboardStorageRule()

    @Test
    fun `a preset can be chosen and it's stored as a setting`() = runBlocking {
        storage.activateCharacterLayout(Presets.ENGLISH.id)
        assertEquals(Presets.ENGLISH.id, storage.settings.characterLayoutId.first())
        assertEquals(Presets.ENGLISH, storage.getActiveLayouts().first().characters)
    }

    @Test
    fun `a user layout can be chosen`() = runBlocking {
        val mine = Presets.SPANISH.copyAsUser(LayoutId("user.es"), "Mi castellano")
        storage.layouts.save(mine)
        storage.activateCharacterLayout(mine.id)
        assertEquals(mine, storage.getActiveLayouts().first().characters)
    }

    @Test
    fun `a layout that doesn't exist can't be chosen and nothing is stored`() = runBlocking {
        val failure = runCatching { storage.activateCharacterLayout(LayoutId("user.nope")) }.exceptionOrNull()
        assertEquals(IllegalArgumentException::class, failure?.let { it::class })
        assertNull(storage.settings.characterLayoutId.first())
    }

    @Test
    fun `the language and the functions are chosen independently`() = runBlocking {
        val myFunctions = Presets.DEFAULT_FUNCTIONS.copyAsUser(LayoutId("user.functions"), "Mis funciones")
        storage.layouts.save(myFunctions)
        storage.activateFunctionLayout(myFunctions.id)
        storage.activateCharacterLayout(Presets.ENGLISH.id)
        val active = storage.getActiveLayouts().first()
        assertEquals(myFunctions, active.functions)
        assertEquals(Presets.ENGLISH, active.characters)
    }
}
