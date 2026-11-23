package pol.rubiano.eyesensekeyboard.hexgrid.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun `the label centre of each corner key returns that corner`() {
        CornerPosition.entries.forEach { position ->
            val labelCenter = geometry.outline(PanelSlot.Corner(position)).labelCenter
            assertEquals(PanelSlot.Corner(position), geometry.slotAt(labelCenter))
        }
    }

    @Test
    fun `the half cell beside the end of a row belongs to the corner key`() {
        // Midpoint of the top-left edge of the first hexagon in the E row, nudged a little outwards:
        // it falls in the half cell (-4, -1), cut by the left edge.
        val corners = geometry.hexCorners(HexCoord(-3, 0))
        val edgeMid = towards(corners[4], corners[5], 0.5f)
        val nudge = 0.05f * geometry.hexRadius
        val point = Point(edgeMid.x - 0.5f * nudge, edgeMid.y - 0.866f * nudge)

        assertFalse(geometry.pixelToHex(point) in panel) // it really is outside the honeycomb
        assertEquals(PanelSlot.Corner(CornerPosition.TOP_LEFT), geometry.slotAt(point))
    }

    @Test
    fun `a nook in the top zigzag goes to the nearest hexagon`() {
        // Right on the top edge, between the peaks of (0,-2) and (1,-2).
        val between = geometry.center(HexCoord(0, -2)).x + geometry.hexWidth / 2
        val nook = Point(between, 1f)

        assertNull(panel.slotOf(geometry.pixelToHex(nook))) // nobody owns that cell
        val slot = geometry.slotAt(nook)
        assertTrue(slot == PanelSlot.Cell(HexCoord(0, -2)) || slot == PanelSlot.Cell(HexCoord(1, -2)))
    }

    @Test
    fun `tapping any part of merged hexagons returns the merged slot`() {
        val bar = setOf(HexCoord(0, 2), HexCoord(1, 2))
        val merged = PanelGeometry.fit(panel.copy(merged = listOf(bar)), 1080f, 840f)
        bar.forEach { assertEquals(PanelSlot.Merged(bar), merged.slotAt(merged.center(it))) }
        // Right on the old wall between them, too.
        val (a, b) = bar.map { merged.center(it) }
        assertEquals(PanelSlot.Merged(bar), merged.slotAt(towards(a, b, 0.5f)))
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
