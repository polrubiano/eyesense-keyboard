package pol.rubiano.eyesensekeyboard.hexgrid.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class HitTestTest {

    private val panel = HexPanel(rowsAroundCenter = 2, rings = 3)
    private val geometry = PanelGeometry.fit(panel, 1080f, 840f)

    private fun towards(from: Point, to: Point, fraction: Float) = Point(
        from.x + (to.x - from.x) * fraction,
        from.y + (to.y - from.y) * fraction,
    )

    @Test
    fun `the centre of every hexagon returns that hexagon - inverse of step 3`() {
        panel.cells.forEach { cell ->
            assertEquals(PanelSlot.Cell(cell), geometry.slotAt(geometry.center(cell)))
        }
    }

    @Test
    fun `near any vertex, still inside, it's still the same hexagon`() {
        panel.cells.forEach { cell ->
            val c = geometry.center(cell)
            geometry.hexCorners(cell).forEach { vertex ->
                assertEquals(PanelSlot.Cell(cell), geometry.slotAt(towards(c, vertex, 0.95f)))
            }
        }
    }

    @Test
    fun `the corner of the E's rectangle is NOT the E - it's the N above it`() {
        // This is the XML problem: rectangular zones overlapping. Here the zone is hexagonal.
        val e = geometry.center(HexCoord.ORIGIN)
        val cornerOfBoundingBox = Point(e.x - geometry.hexWidth / 2, e.y - geometry.hexRadius)
        assertEquals(PanelSlot.Cell(HexCoord(0, -1)), geometry.slotAt(cornerOfBoundingBox))
    }

    @Test
    fun `the inside of each triangle returns its corner`() {
        CornerPosition.entries.forEach { position ->
            val incenter = geometry.outline(PanelSlot.Corner(position)).incenter
            assertEquals(PanelSlot.Corner(position), geometry.slotAt(incenter))
        }
    }

    @Test
    fun `a nook between the triangle and the honeycomb goes to the nearest hexagon`() {
        // Midpoint of the top-left edge of the first hexagon in the E row, nudged a little outwards.
        val corners = geometry.hexCorners(HexCoord(-3, 0))
        val edgeMid = towards(corners[4], corners[5], 0.5f)
        val nudge = 0.05f * geometry.hexRadius
        val nook = Point(edgeMid.x - 0.5f * nudge, edgeMid.y - 0.866f * nudge)

        assertFalse(geometry.pixelToHex(nook) in panel) // it really is outside the honeycomb
        assertEquals(PanelSlot.Cell(HexCoord(-3, 0)), geometry.slotAt(nook))
    }

    @Test
    fun `outside the panel there's nothing`() {
        assertNull(geometry.slotAt(Point(-1f, 10f)))
        assertNull(geometry.slotAt(Point(10f, geometry.height + 1f)))
    }

    @Test
    fun `every point of the panel lands in some slot - no dead zones`() {
        val step = 7f
        var y = 0f
        while (y <= geometry.height) {
            var x = 0f
            while (x <= geometry.width) {
                // If any point returned null, the assertion would fail on that point.
                assertEquals(true, geometry.slotAt(Point(x, y)) != null)
                x += step
            }
            y += step
        }
    }
}
