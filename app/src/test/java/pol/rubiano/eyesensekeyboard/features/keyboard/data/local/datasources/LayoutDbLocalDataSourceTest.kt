package pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datasources

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.db.KeyboardDatabase
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAssignment
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexCoord

/** The whole path, domain → mappers → DAO → Room → back, with a real in-memory database. */
@RunWith(RobolectricTestRunner::class)
class LayoutDbLocalDataSourceTest {

    private val db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        KeyboardDatabase::class.java,
    ).allowMainThreadQueries().build()

    private val source = LayoutDbLocalDataSource(db.layoutDao())

    @After
    fun close() = db.close()

    private val mySpanish = Presets.SPANISH
        .copyAsUser(LayoutId("user.es"), "Castellano 2")
        .assign(HexCoord.ORIGIN, KeyAssignment.Text("a"))

    @Test
    fun `a saved layout is read back exactly as it was`() = runBlocking {
        source.save(mySpanish)
        assertEquals(listOf(mySpanish), source.characterLayouts.first())
    }

    @Test
    fun `an edited layout replaces the saved one`() = runBlocking {
        source.save(mySpanish)
        val edited = mySpanish.clear(HexCoord.ORIGIN)
        source.save(edited)
        assertEquals(listOf(edited), source.characterLayouts.first())
    }

    @Test
    fun `function layouts make the same trip`() = runBlocking {
        val myFunctions = Presets.DEFAULT_FUNCTIONS.copyAsUser(LayoutId("user.f"), "Mis funciones")
        source.save(myFunctions)
        assertEquals(listOf(myFunctions), source.functionLayouts.first())
        source.deleteFunctionLayout(myFunctions.id)
        assertEquals(emptyList<Any>(), source.functionLayouts.first())
    }
}
