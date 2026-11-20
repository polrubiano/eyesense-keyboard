package pol.rubiano.eyesensekeyboard.hexgrid.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HexPanelTest {

    /** The EyeSense keyboard silhouette: 5 rows, rings 0 to 3. */
    private val panel = HexPanel(rowsAroundCenter = 2, rings = 3)

    @Test
    fun `the keyboard has 29 hexagons in rows of 5-6-7-6-5`() {
        assertEquals(29, panel.cells.size)
        val perRow = panel.cells.groupingBy { it.r }.eachCount().toSortedMap()
        assertEquals(listOf(5, 6, 7, 6, 5), perRow.values.toList())
    }

    @Test
    fun `the full rings stay whole and ring 3 only keeps its sides`() {
        val perRing = panel.cells.groupingBy { it.ring }.eachCount()
        assertEquals(mapOf(0 to 1, 1 to 6, 2 to 12, 3 to 10), perRing)
    }

    @Test
    fun `the E row has 3 hexagons on each side`() {
        val centralRow = panel.cells.filter { it.r == 0 }.map { it.q }.sorted()
        assertEquals((-3..3).toList(), centralRow)
    }

    @Test
    fun `the hexagons come ordered by closeness, starting at the centre`() {
        assertEquals(HexCoord.ORIGIN, panel.cells.first())
        val rings = panel.cells.map { it.ring }
        assertEquals(rings.sorted(), rings)
    }

    @Test
    fun `33 slots in total - 29 hexagons and 4 different triangles`() {
        assertEquals(33, panel.slots.size)
        assertEquals(CornerPosition.entries.toSet(), panel.corners.map { it.position }.toSet())
    }

    @Test
    fun `contains tells apart the panel's hexagons from the ones that go beyond it`() {
        assertTrue(HexCoord.ORIGIN in panel)
        assertTrue(HexCoord(3, 0) in panel)   // end of the E row
        assertFalse(HexCoord(4, 0) in panel)  // ring 4: too far
        assertFalse(HexCoord(0, 3) in panel)  // row 3: outside the 5 rows
    }
}
