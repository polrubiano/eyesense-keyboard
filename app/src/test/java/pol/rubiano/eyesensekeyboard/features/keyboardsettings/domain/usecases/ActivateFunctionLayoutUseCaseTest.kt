package pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.usecases

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pol.rubiano.eyesensekeyboard.app.KeyboardStorageRule
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets

@RunWith(RobolectricTestRunner::class)
class ActivateFunctionLayoutUseCaseTest {

    @get:Rule
    val storage = KeyboardStorageRule()

    @Test
    fun `a user function layout can be chosen`() = runBlocking {
        val mine = Presets.DEFAULT_FUNCTIONS.copyAsUser(LayoutId("user.functions"), "Mis funciones")
        storage.layouts.save(mine)
        storage.activateFunctionLayout(mine.id)
        assertEquals(mine.id, storage.settings.functionLayoutId.first())
        assertEquals(mine, storage.getActiveLayouts().first().functions)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a function layout that doesn't exist can't be chosen`() = runBlocking {
        storage.activateFunctionLayout(LayoutId("user.nope"))
    }
}
