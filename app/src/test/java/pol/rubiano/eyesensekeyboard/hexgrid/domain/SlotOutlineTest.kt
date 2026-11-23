package pol.rubiano.eyesensekeyboard.hexgrid.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt

class SlotOutlineTest {

    private val geometry = PanelGeometry.fit(HexPanel(2, 3), 1080f, 840f)
    private val hexArea = 3f * sqrt(3f) / 2f * geometry.hexRadius * geometry.hexRadius
    private val tolerance = 0.01f

    /** Distance from point p to the straight line through a and b. */
    private fun distanceToLine(p: Point, a: Point, b: Point): Float =
        abs((b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x)) / hypot(b.x - a.x, b.y - a.y)

    @Test
    fun `a hexagon's label circle is centred on it and touches its 6 sides`() {
        val outline = geometry.outline(PanelSlot.Cell(HexCoord(1, -1)))
        assertEquals(geometry.center(HexCoord(1, -1)), outline.labelCenter)
        val v = outline.vertices
        v.indices.forEach { i ->
            assertEquals(outline.labelRadius, distanceToLine(outline.labelCenter, v[i], v[(i + 1) % v.size]), tolerance)
        }
    }

    @Test
    fun `a corner key is one and a half cells, labelled on the whole one`() {
        val outline = geometry.outline(PanelSlot.Corner(CornerPosition.TOP_RIGHT))
        assertEquals(1.5f * hexArea, outline.vertices.signedArea(), 0.5f)
        assertEquals(geometry.center(HexCoord(4, -2)), outline.labelCenter)
        assertEquals(geometry.hexWidth / 2f, outline.labelRadius, tolerance)
    }

    @Test
    fun `corner keys stay inside the panel`() {
        CornerPosition.entries.forEach { position ->
            val vertices = geometry.outline(PanelSlot.Corner(position)).vertices
            assertTrue(vertices.all { it.x in -tolerance..geometry.width + tolerance })
            assertTrue(vertices.all { it.y in -tolerance..geometry.height + tolerance })
        }
    }

    @Test
    fun `inset moves every side inwards by the same distance, corner keys included`() {
        val slots = listOf(PanelSlot.Cell(HexCoord.ORIGIN), PanelSlot.Corner(CornerPosition.TOP_LEFT))
        slots.forEach { slot ->
            val original = geometry.outline(slot)
            val shrunk = original.inset(5f)
            assertEquals(original.labelRadius - 5f, shrunk.labelRadius, tolerance)
            val v = original.vertices
            v.indices.forEach { i ->
                val a = v[i]
                val b = v[(i + 1) % v.size]
                assertEquals(5f, distanceToLine(shrunk.vertices[i], a, b), tolerance)
            }
        }
    }
}
