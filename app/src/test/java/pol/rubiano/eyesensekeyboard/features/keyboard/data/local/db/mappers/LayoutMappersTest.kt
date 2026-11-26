package pol.rubiano.eyesensekeyboard.features.keyboard.data.local.db.mappers

import org.junit.Assert.assertEquals
import org.junit.Test
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.CharacterKeyEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.CharacterLayoutEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.CharacterLayoutWithKeys
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.FunctionKeyEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.FunctionLayoutEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.FunctionLayoutWithKeys
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.KeyKind
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.CharacterLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Emotion
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAction
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAssignment
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.Presets
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexCoord

class LayoutMappersTest {

    private val mySpanish = Presets.SPANISH.copyAsUser(LayoutId("user.es"), "Castellano 2")
    private val myEnglish = Presets.ENGLISH.copyAsUser(LayoutId("user.en"), "English 2")
    private val myFunctions = Presets.DEFAULT_FUNCTIONS.copyAsUser(LayoutId("user.f"), "Mis funciones")

    // ---- round trips: what goes in comes out the same ----

    @Test
    fun `a spanish copy comes back the same, space bar included`() {
        assertEquals(mySpanish, mySpanish.toEntity().toDomain())
    }

    @Test
    fun `an english copy comes back the same, with its three-hexagon space bar`() {
        assertEquals(myEnglish, myEnglish.toEntity().toDomain())
    }

    @Test
    fun `a function on a hexagon comes back as a function`() {
        val edited = mySpanish.assign(HexCoord.ORIGIN, KeyAssignment.Function(KeyAction.Speak))
        assertEquals(edited, edited.toEntity().toDomain())
    }

    @Test
    fun `a function layout comes back the same`() {
        assertEquals(myFunctions, myFunctions.toEntity().toDomain())
    }

    // ---- what the rows look like ----

    @Test
    fun `one row per hexagon, and the space bar hexagons share a group`() {
        val stored = mySpanish.toEntity()
        assertEquals(mySpanish.cells.size, stored.keys.size)
        val spaceBar = stored.keys.filter { it.mergedGroup != null }
        assertEquals(setOf(HexCoord(0, 2), HexCoord(1, 2)), spaceBar.map { HexCoord(it.q, it.r) }.toSet())
        assertEquals(1, spaceBar.map { it.mergedGroup }.distinct().size)
    }

    @Test
    fun `functions are stored by their stable id, corners by name`() {
        val stored = myFunctions.toEntity()
        assertEquals(FunctionKeyEntity("user.f", "BOTTOM_RIGHT", "speak"), stored.keys.single { it.corner == "BOTTOM_RIGHT" })
    }

    // ---- what can't be stored ----

    @Test(expected = IllegalArgumentException::class)
    fun `a preset can't be stored`() {
        Presets.SPANISH.toEntity()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a menu choice can't be stored`() {
        mySpanish.assign(HexCoord.ORIGIN, KeyAssignment.Choice(Emotion.HAPPY)).toEntity()
    }

    // ---- what can't be read ----

    @Test(expected = IllegalArgumentException::class)
    fun `an unknown function id fails when read`() {
        CharacterLayoutWithKeys(
            CharacterLayoutEntity("user.x", "Roto"),
            listOf(CharacterKeyEntity("user.x", 0, 0, KeyKind.FUNCTION, "teleport", null)),
        ).toDomain()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `an unknown corner fails when read`() {
        FunctionLayoutWithKeys(
            FunctionLayoutEntity("user.x", "Roto"),
            listOf(FunctionKeyEntity("user.x", "MIDDLE", "speak")),
        ).toDomain()
    }

    @Test
    fun `what is read is always a user layout`() {
        val read: CharacterLayout = mySpanish.toEntity().toDomain()
        assertEquals(mySpanish.origin, read.origin)
    }
}
