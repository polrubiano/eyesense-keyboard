package pol.rubiano.eyesensekeyboard.features.keyboardsettings.domain.usecases

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pol.rubiano.eyesensekeyboard.app.KeyboardStorageRule
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAssignment
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexCoord

/** The user's choice looked up among the layouts, on real storage. */
@RunWith(RobolectricTestRunner::class)
class GetActiveLayoutsUseCaseTest {

    @get:Rule
    val storage = KeyboardStorageRule()

    private suspend fun active() = storage.getActiveLayouts().first()

    private val mySpanish = Presets.SPANISH
        .copyAsUser(LayoutId("user.es"), "Castellano (mío)")
        .assign(HexCoord.ORIGIN, KeyAssignment.Text("a"))

    @Test
    fun `with nothing chosen, the presets are in use`() = runBlocking {
        assertEquals(Presets.SPANISH, active().characters)
        assertEquals(Presets.DEFAULT_FUNCTIONS, active().functions)
    }

    @Test
    fun `a chosen user layout is the one in use`() = runBlocking {
        storage.layouts.save(mySpanish)
        storage.settings.setCharacterLayoutId(mySpanish.id)
        assertEquals(mySpanish, active().characters)
    }

    @Test
    fun `editing the chosen layout shows the edit right away`() = runBlocking {
        storage.layouts.save(mySpanish)
        storage.settings.setCharacterLayoutId(mySpanish.id)
        val edited = mySpanish.assign(HexCoord.ORIGIN, KeyAssignment.Text("o"))
        storage.layouts.save(edited)
        assertEquals(edited, active().characters)
    }

    @Test
    fun `deleting the chosen layout falls back to the preset`() = runBlocking {
        storage.layouts.save(mySpanish)
        storage.settings.setCharacterLayoutId(mySpanish.id)
        storage.layouts.deleteCharacterLayout(mySpanish.id)
        assertEquals(Presets.SPANISH, active().characters)
    }

    @Test
    fun `a chosen id that points to nothing falls back to the preset`() = runBlocking {
        storage.settings.setFunctionLayoutId(LayoutId("user.gone"))
        assertEquals(Presets.DEFAULT_FUNCTIONS, active().functions)
    }
}
