package pol.rubiano.eyesensekeyboard.hexgrid.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt

class PolygonTest {

    private val geometry = PanelGeometry.fit(HexPanel(2, 3), 1080f, 840f)
    private val hexArea = 3f * sqrt(3f) / 2f * geometry.hexRadius * geometry.hexRadius
    private val tolerance = 0.01f

    // Cells just outside the right edge of the top rows: what the top-right corner key will be made of.
    private val missingCellRow2 = HexCoord(4, -2) // fits whole inside the panel
    private val halfCellRow1 = HexCoord(4, -1)    // cut in half by the right edge

    private fun assertSamePoint(expected: Point, actual: Point) {
        assertEquals(expected.x, actual.x, tolerance)
        assertEquals(expected.y, actual.y, tolerance)
    }

    private fun distanceToLine(p: Point, a: Point, b: Point): Float =
        abs((b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x)) / hypot(b.x - a.x, b.y - a.y)

    // --- Point arithmetic ---

    @Test
    fun `adding a path moves the point and subtracting gives the path back`() {
        val from = Point(1f, 2f)
        val to = Point(4f, 6f)
        assertEquals(Point(3f, 4f), to - from)
        assertEquals(to, from + (to - from))
    }

    @Test
    fun `half the path from one point to another is the midpoint`() {
        val from = Point(0f, 0f)
        val to = Point(4f, -2f)
        assertEquals(Point(2f, -1f), from + (to - from) * 0.5f)
    }

    @Test
    fun `distance is the straight-line length, the same both ways`() {
        val a = Point(0f, 0f)
        val b = Point(3f, 4f)
        assertEquals(5f, a.distanceTo(b), tolerance)
        assertEquals(a.distanceTo(b), b.distanceTo(a), tolerance)
    }

    @Test
    fun `around treats the list as a ring`() {
        val vertices = listOf("a", "b", "c", "d")
        assertEquals(Triple("d", "a", "b"), vertices.around(0))
        assertEquals(Triple("a", "b", "c"), vertices.around(1))
        assertEquals(Triple("c", "d", "a"), vertices.around(3))
    }

    // --- ② clippedToPanel ---

    @Test
    fun `a cell inside the panel isn't changed by the clip`() {
        val outline = geometry.regionOutline(setOf(missingCellRow2))
        assertEquals(outline.signedArea(), geometry.clippedToPanel(outline).signedArea(), 0.5f)
    }

    @Test
    fun `a cell cut by the edge keeps only its inner half`() {
        val clipped = geometry.clippedToPanel(geometry.regionOutline(setOf(halfCellRow1)))
        assertEquals(hexArea / 2f, clipped.signedArea(), 0.5f)
        assertTrue(clipped.all { it.x <= geometry.width + tolerance })
    }

    @Test
    fun `the future corner key is one and a half cells`() {
        val corner = geometry.clippedToPanel(geometry.regionOutline(setOf(missingCellRow2, halfCellRow1)))
        assertEquals(1.5f * hexArea, corner.signedArea(), 0.5f)
    }

    // --- ③ insetBy ---

    @Test
    fun `on a single hexagon it matches the old inset`() {
        val hexagon = geometry.outline(PanelSlot.Cell(HexCoord(1, -1)))
        val old = hexagon.inset(5f).vertices
        val new = hexagon.vertices.insetBy(5f)
        old.zip(new).forEach { (expected, actual) -> assertSamePoint(expected, actual) }
    }

    @Test
    fun `every side moves inwards by exactly the distance, nooks included`() {
        // Two neighbours: their outline has two inward nooks where the wall used to be.
        val original = geometry.regionOutline(setOf(HexCoord.ORIGIN, HexCoord(0, 1)))
        val shrunk = original.insetBy(5f)
        val n = original.size
        original.indices.forEach { i ->
            val a = original[i]
            val b = original[(i + 1) % n]
            assertEquals(5f, distanceToLine(shrunk[i], a, b), tolerance)
            assertEquals(5f, distanceToLine(shrunk[(i + 1) % n], a, b), tolerance)
        }
        assertTrue(shrunk.signedArea() < original.signedArea())
    }

    @Test
    fun `the clipped corner key can be shrunk too`() {
        val corner = geometry.clippedToPanel(geometry.regionOutline(setOf(missingCellRow2, halfCellRow1)))
        val shrunk = corner.insetBy(5f)
        assertTrue(shrunk.all { it.x <= geometry.width - 5f + tolerance && it.y >= 5f - tolerance })
    }
}
