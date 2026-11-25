package pol.rubiano.eyesensekeyboard.keyboard.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pol.rubiano.eyesensekeyboard.hexgrid.domain.CornerPosition
import pol.rubiano.eyesensekeyboard.hexgrid.domain.HexCoord
import pol.rubiano.eyesensekeyboard.hexgrid.domain.PanelSlot

class MenuTest {

    private val keyboard = Keyboard(Presets.PANEL, Presets.SPANISH, Presets.DEFAULT_FUNCTIONS)

    private val emotions = keyboard.showing(Menu.Emotions)

    @Test
    fun `the centre is none`() {
        assertEquals(KeyAssignment.Choice(null), emotions.keyAt(PanelSlot.Cell(HexCoord.ORIGIN)))
    }

    @Test
    fun `the options follow the first ring, in order`() {
        // The spiral starts the ring east and turns clockwise: E, SE, SW…
        assertEquals(KeyAssignment.Choice(Emotion.HAPPY), emotions.keyAt(PanelSlot.Cell(HexCoord(1, 0))))
        assertEquals(KeyAssignment.Choice(Emotion.SAD), emotions.keyAt(PanelSlot.Cell(HexCoord(0, 1))))
        assertEquals(KeyAssignment.Choice(Emotion.ANGRY), emotions.keyAt(PanelSlot.Cell(HexCoord(-1, 1))))
    }

    @Test
    fun `the rest of the hexagons are free`() {
        assertEquals(1 + Emotion.entries.size, emotions.characters.cells.size)
    }

    @Test
    fun `the space bar splits back into hexagons`() {
        assertTrue(emotions.panel.merged.isEmpty())
    }

    @Test
    fun `only the opener's corner stays`() {
        assertEquals(
            mapOf(CornerPosition.BOTTOM_LEFT to KeyAction.SelectEmotion),
            emotions.functions.corners,
        )
    }

    @Test
    fun `the opener's corner stays wherever the user put it`() {
        val moved = Presets.DEFAULT_FUNCTIONS
            .copyAsUser(LayoutId("user.functions"), "Mis funciones")
            .swap(CornerPosition.BOTTOM_LEFT, CornerPosition.TOP_RIGHT)
        val menu = keyboard.withFunctions(moved).showing(Menu.Emotions)
        assertEquals(mapOf(CornerPosition.TOP_RIGHT to KeyAction.SelectEmotion), menu.functions.corners)
        assertNull(menu.keyAt(PanelSlot.Corner(CornerPosition.BOTTOM_LEFT)))
    }

    @Test
    fun `each menu opens with its opener`() {
        listOf<Menu>(Menu.Emotions).forEach { menu -> assertEquals(menu, menu.opener.menu()) }
    }

    @Test
    fun `every option has its own pictogram`() {
        val pictograms = Menu.Emotions.options.map { it.pictogram }
        assertTrue(pictograms.all { it.isNotBlank() })
        assertEquals(pictograms.size, pictograms.toSet().size)
    }

    @Test
    fun `the keyboard it came from doesn't change`() {
        assertEquals(KeyAssignment.Text("e"), keyboard.keyAt(PanelSlot.Cell(HexCoord.ORIGIN)))
    }

    @Test
    fun `emotions have unique, stable and recoverable ids`() {
        val ids = Emotion.entries.map { it.id }
        assertEquals(listOf("happy", "angry", "sad"), ids)
        Emotion.entries.forEach { assertEquals(it, Emotion.fromId(it.id)) }
    }
}
