package pol.rubiano.eyesensekeyboard.features.keyboard.data.datarepositories

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datasources.LayoutDbLocalDataSource
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datastore.ActiveLayoutDataStoreManager
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.db.KeyboardDatabase
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexCoord
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAssignment
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutOrigin
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import java.io.File

/**
 * The rules, checked against the real storage: Room in memory and DataStore in a temporary file.
 * Each test starts with both empty. Storage on its own is tested in LayoutDbLocalDataSourceTest
 * and ActiveLayoutDataStoreManagerTest.
 */
@RunWith(RobolectricTestRunner::class)
class LayoutDataRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        KeyboardDatabase::class.java,
    ).allowMainThreadQueries().build()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Lazy: the temporary folder only exists once the test is running, not when the class is built.
    private val repository by lazy {
        LayoutDataRepository(
            LayoutDbLocalDataSource(db.layoutDao()),
            ActiveLayoutDataStoreManager(
                PreferenceDataStoreFactory.create(scope = scope, produceFile = { File(folder.root, "settings.preferences_pb") }),
            ),
        )
    }

    @After
    fun tearDown() {
        scope.cancel()
        db.close()
    }

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

    // ---- the active layout ----

    @Test
    fun `with nothing activated, the presets are in use`() = runBlocking {
        assertEquals(Presets.SPANISH, repository.activeCharacterLayout.first())
        assertEquals(Presets.DEFAULT_FUNCTIONS, repository.activeFunctionLayout.first())
    }

    @Test
    fun `an activated user layout is the one in use`() = runBlocking {
        repository.save(mySpanish)
        repository.activateCharacterLayout(mySpanish.id)
        assertEquals(mySpanish, repository.activeCharacterLayout.first())
    }

    @Test
    fun `a preset can be activated too`() = runBlocking {
        repository.activateCharacterLayout(Presets.ENGLISH.id)
        assertEquals(Presets.ENGLISH, repository.activeCharacterLayout.first())
    }

    @Test
    fun `editing the active layout shows the edit right away`() = runBlocking {
        repository.save(mySpanish)
        repository.activateCharacterLayout(mySpanish.id)
        val edited = mySpanish.assign(HexCoord.ORIGIN, KeyAssignment.Text("o"))
        repository.save(edited)
        assertEquals(edited, repository.activeCharacterLayout.first())
    }

    @Test
    fun `deleting the active layout falls back to the preset`() = runBlocking {
        repository.save(mySpanish)
        repository.activateCharacterLayout(mySpanish.id)
        repository.deleteCharacterLayout(mySpanish.id)
        assertEquals(Presets.SPANISH, repository.activeCharacterLayout.first())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a layout that doesn't exist can't be activated`() = runBlocking {
        repository.activateCharacterLayout(LayoutId("user.nope"))
    }

    @Test
    fun `the language and the functions are activated independently`() = runBlocking {
        val myFunctions = Presets.DEFAULT_FUNCTIONS.copyAsUser(LayoutId("user.functions"), "Mis funciones")
        repository.save(myFunctions)
        repository.activateFunctionLayout(myFunctions.id)
        repository.activateCharacterLayout(Presets.ENGLISH.id)
        assertEquals(myFunctions, repository.activeFunctionLayout.first())
        assertEquals(Presets.ENGLISH, repository.activeCharacterLayout.first())
    }
}
