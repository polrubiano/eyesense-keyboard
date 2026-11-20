package pol.rubiano.eyesensekeyboard.hexgrid.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.abs
import kotlin.math.hypot

class SlotOutlineTest {

    private val geometry = PanelGeometry.fit(HexPanel(2, 3), 1080f, 840f)
    private val tolerance = 0.01f

    /** Distance from point p to the straight line through a and b. */
    private fun distanceToLine(p: Point, a: Point, b: Point): Float =
        abs((b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x)) / hypot(b.x - a.x, b.y - a.y)

    private fun assertInscribedCircleTouchesEverySide(outline: SlotOutline) {
        val v = outline.vertices
        v.indices.forEach { i ->
            val a = v[i]
            val b = v[(i + 1) % v.size]
            assertEquals(outline.inradius, distanceToLine(outline.incenter, a, b), tolerance)
        }
    }

    @Test
    fun `a hexagon's inscribed circle is centred on it and touches its 6 sides`() {
        val outline = geometry.outline(PanelSlot.Cell(HexCoord(1, -1)))
        assertEquals(geometry.center(HexCoord(1, -1)), outline.incenter)
        assertInscribedCircleTouchesEverySide(outline)
    }

    @Test
    fun `a triangle's inscribed circle touches its 3 sides`() {
        CornerPosition.entries.forEach { position ->
            assertInscribedCircleTouchesEverySide(geometry.outline(PanelSlot.Corner(position)))
        }
    }

    @Test
    fun `inset moves every side inwards by the same distance`() {
        val slots = listOf(PanelSlot.Cell(HexCoord.ORIGIN), PanelSlot.Corner(CornerPosition.TOP_LEFT))
        slots.forEach { slot ->
            val original = geometry.outline(slot)
            val shrunk = original.inset(5f)
            assertEquals(original.inradius - 5f, shrunk.inradius, tolerance)
            // The shrunk shape still has its circle touching every side → the gap is even.
            assertInscribedCircleTouchesEverySide(shrunk)
        }
    }
}
