package pol.rubiano.eyesensekeyboard.hexgrid.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class PanelGeometryTest {

    private val panel = HexPanel(rowsAroundCenter = 2, rings = 3)

    /** Typical phone in portrait: 1080 px wide, bottom 35% ≈ 840 px tall. */
    private val geometry = PanelGeometry.fit(panel, availableWidth = 1080f, availableHeight = 840f)

    private val tolerance = 0.01f

    private fun assertPoint(expected: Point, actual: Point) {
        assertEquals(expected.x, actual.x, tolerance)
        assertEquals(expected.y, actual.y, tolerance)
    }

    @Test
    fun `in portrait the width governs - the panel fills the width and has height to spare`() {
        assertEquals(1080f, geometry.width, tolerance)
        assertTrue(geometry.height < 840f)
    }

    @Test
    fun `in a very wide space the height governs`() {
        val wide = PanelGeometry.fit(panel, availableWidth = 3000f, availableHeight = 500f)
        assertEquals(500f, wide.height, tolerance)
        assertTrue(wide.width < 3000f)
    }

    @Test
    fun `the E sits right in the middle of the panel`() {
        assertPoint(Point(geometry.width / 2, geometry.height / 2), geometry.center(HexCoord.ORIGIN))
    }

    @Test
    fun `the 6 neighbours of any hexagon are at the same distance - one hexagon width`() {
        val from = HexCoord(1, -1)
        val c = geometry.center(from)
        from.neighbors().forEach { n ->
            val p = geometry.center(n)
            assertEquals(geometry.hexWidth, hypot(p.x - c.x, p.y - c.y), tolerance)
        }
    }

    @Test
    fun `the honeycomb touches the 4 edges of the panel`() {
        val r = geometry.hexRadius
        val w = geometry.hexWidth
        assertEquals(0f, geometry.center(HexCoord(-3, 0)).x - w / 2, tolerance)               // left
        assertEquals(geometry.width, geometry.center(HexCoord(3, 0)).x + w / 2, tolerance)    // right
        assertEquals(0f, geometry.center(HexCoord(0, -2)).y - r, tolerance)                   // top
        assertEquals(geometry.height, geometry.center(HexCoord(0, 2)).y + r, tolerance)       // bottom
    }

    @Test
    fun `the top-left triangle is pinned to the top-left corner and its point aims at the E row`() {
        val (corner, onTopEdge, tip) = geometry.triangle(CornerPosition.TOP_LEFT)
        assertPoint(Point(0f, 0f), corner)
        assertEquals(0f, onTopEdge.y, tolerance)
        assertEquals(0f, tip.x, tolerance)
        assertEquals(geometry.height / 2 - geometry.hexRadius / 2, tip.y, tolerance)
    }

    @Test
    fun `the long side of the triangle touches the outer vertex of each row`() {
        val (_, a, b) = geometry.triangle(CornerPosition.TOP_LEFT)
        // First hexagon of rows 0, -1 and -2; vertex 4 = top-left.
        val outerVertices = listOf(HexCoord(-3, 0), HexCoord(-2, -1), HexCoord(-1, -2))
            .map { geometry.hexCorners(it)[4] }

        outerVertices.forEach { p ->
            // Cross product ≈ 0 ⇔ the point is on the line a-b.
            val cross = (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x)
            assertEquals(0f, cross, 1f)
        }
    }

    @Test
    fun `the 4 triangles are mirror images of each other`() {
        val tl = geometry.triangle(CornerPosition.TOP_LEFT)
        val tr = geometry.triangle(CornerPosition.TOP_RIGHT)
        val bl = geometry.triangle(CornerPosition.BOTTOM_LEFT)
        val br = geometry.triangle(CornerPosition.BOTTOM_RIGHT)
        tl.indices.forEach { i ->
            assertPoint(Point(geometry.width - tl[i].x, tl[i].y), tr[i])
            assertPoint(Point(tl[i].x, geometry.height - tl[i].y), bl[i])
            assertPoint(Point(geometry.width - tl[i].x, geometry.height - tl[i].y), br[i])
        }
    }
}
