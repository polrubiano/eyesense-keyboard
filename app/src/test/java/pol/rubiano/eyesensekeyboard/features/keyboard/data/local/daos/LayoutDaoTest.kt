package pol.rubiano.eyesensekeyboard.features.keyboard.data.local.daos

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.db.KeyboardDatabase
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.CharacterKeyEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.CharacterLayoutEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.CharacterLayoutWithKeys
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.FunctionKeyEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.FunctionLayoutEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.FunctionLayoutWithKeys
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.KeyKind

/** The real Room database, in memory, on the JVM (Robolectric): no phone needed, runs in CI. */
@RunWith(RobolectricTestRunner::class)
class LayoutDaoTest {

    private val db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        KeyboardDatabase::class.java,
    ).allowMainThreadQueries().build()

    private val dao = db.layoutDao()

    @After
    fun close() = db.close()

    private fun characters(id: String, name: String, vararg keys: CharacterKeyEntity) =
        CharacterLayoutWithKeys(CharacterLayoutEntity(id, name), keys.toList())

    private fun text(layoutId: String, q: Int, r: Int, value: String, group: Int? = null) =
        CharacterKeyEntity(layoutId, q, r, KeyKind.TEXT, value, group)

    private val spanish = characters(
        "user.1", "Castellano 2",
        text("user.1", 0, 0, "e"),
        text("user.1", 0, 2, " ", group = 0),
        text("user.1", 1, 2, " ", group = 0),
        CharacterKeyEntity("user.1", 1, -1, KeyKind.FUNCTION, "speak", null),
    )

    @Test
    fun `it starts empty`() = runBlocking {
        assertEquals(emptyList<CharacterLayoutWithKeys>(), dao.characterLayouts().first())
        assertEquals(emptyList<FunctionLayoutWithKeys>(), dao.functionLayouts().first())
    }

    @Test
    fun `a saved layout comes back with all its keys`() = runBlocking {
        dao.save(spanish)
        val saved = dao.characterLayouts().first().single()
        assertEquals(spanish.layout, saved.layout)
        assertEquals(spanish.keys.toSet(), saved.keys.toSet())
    }

    @Test
    fun `saving again replaces the layout, and keys that are gone disappear`() = runBlocking {
        dao.save(spanish)
        val edited = characters("user.1", "Castellano rápido", text("user.1", 0, 0, "a"))
        dao.save(edited)
        val saved = dao.characterLayouts().first().single()
        assertEquals("Castellano rápido", saved.layout.name)
        assertEquals(edited.keys, saved.keys)
    }

    @Test
    fun `deleting a layout deletes its keys too`() = runBlocking {
        dao.save(spanish)
        dao.deleteCharacterLayout("user.1")
        assertEquals(emptyList<CharacterLayoutWithKeys>(), dao.characterLayouts().first())
        // The keys don't survive on their own: saving a new layout with the same id starts clean.
        dao.save(characters("user.1", "Nuevo"))
        assertEquals(emptyList<CharacterKeyEntity>(), dao.characterLayouts().first().single().keys)
    }

    @Test
    fun `layouts come sorted by name`() = runBlocking {
        dao.save(characters("user.2", "Zeta"))
        dao.save(characters("user.1", "Alfa"))
        assertEquals(listOf("Alfa", "Zeta"), dao.characterLayouts().first().map { it.layout.name })
    }

    @Test
    fun `whoever is observing sees the change without asking again`() = runBlocking {
        val observed = async { dao.characterLayouts().first { it.isNotEmpty() } }
        dao.save(spanish)
        assertEquals("user.1", withTimeout(5_000) { observed.await() }.single().layout.id)
    }

    @Test
    fun `function layouts keep each corner`() = runBlocking {
        val functions = FunctionLayoutWithKeys(
            FunctionLayoutEntity("user.f", "Mis funciones"),
            listOf(
                FunctionKeyEntity("user.f", "TOP_LEFT", "speak"),
                FunctionKeyEntity("user.f", "BOTTOM_RIGHT", "clear_text"),
            ),
        )
        dao.save(functions)
        assertEquals(functions.keys.toSet(), dao.functionLayouts().first().single().keys.toSet())
        dao.deleteFunctionLayout("user.f")
        assertEquals(emptyList<FunctionLayoutWithKeys>(), dao.functionLayouts().first())
    }
}
