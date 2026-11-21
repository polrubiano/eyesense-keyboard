package pol.rubiano.eyesensekeyboard.keyboard.domain

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pol.rubiano.eyesensekeyboard.hexgrid.domain.HexCoord

class LayoutRepositoryTest {

    private val repository = InMemoryLayoutRepository()

    private val mySpanish = Presets.SPANISH
        .copyAsUser(LayoutId("user.es"), "Castellano (mío)")
        .assign(HexCoord.ORIGIN, KeyAssignment.Text("a"))

    @Test
    fun `at the start it only has the presets`() = runBlocking {
        assertEquals(Presets.characterLayouts, repository.characterLayouts.first())
        assertEquals(Presets.functionLayouts, repository.functionLayouts.first())
    }

    @Test
    fun `saves and recovers a user layout`() = runBlocking {
        repository.save(mySpanish)
        val layouts = repository.characterLayouts.first()
        assertTrue(mySpanish in layouts)
        assertEquals(Presets.characterLayouts.size + 1, layouts.size)
    }

    @Test
    fun `saving again with the same id replaces it`() = runBlocking {
        repository.save(mySpanish)
        val updated = mySpanish.assign(HexCoord.ORIGIN, KeyAssignment.Text("o"))
        repository.save(updated)
        val users = repository.characterLayouts.first().filter { it.origin == LayoutOrigin.USER }
        assertEquals(listOf(updated), users)
    }

    @Test
    fun `deletes a user layout`() = runBlocking {
        repository.save(mySpanish)
        repository.deleteCharacterLayout(mySpanish.id)
        assertEquals(Presets.characterLayouts, repository.characterLayouts.first())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a preset can't be saved`() = runBlocking {
        repository.save(Presets.SPANISH)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a user layout can't take a preset's id`() = runBlocking {
        repository.save(Presets.SPANISH.copyAsUser(Presets.SPANISH.id, "Impostor"))
    }

    @Test
    fun `function layouts are saved the same way`() = runBlocking {
        val myFunctions = Presets.DEFAULT_FUNCTIONS.copyAsUser(LayoutId("user.functions"), "Mis funciones")
        repository.save(myFunctions)
        assertTrue(myFunctions in repository.functionLayouts.first())
    }
}
