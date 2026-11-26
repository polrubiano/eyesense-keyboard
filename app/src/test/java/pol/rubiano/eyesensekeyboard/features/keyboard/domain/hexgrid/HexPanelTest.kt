package pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
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
    fun `33 slots in total - 29 hexagons and 4 different corner keys`() {
        assertEquals(33, panel.slots.size)
        assertEquals(CornerPosition.entries.toSet(), panel.corners.map { it.position }.toSet())
    }

    @Test
    fun `the top-right corner is the whole missing cell plus the half one below it`() {
        assertEquals(setOf(HexCoord(4, -2), HexCoord(4, -1)), panel.cornerCells[CornerPosition.TOP_RIGHT])
    }

    @Test
    fun `the 4 corners are mirror images of each other`() {
        // Mirroring left↔right turns (q, r) into (-q - r, r); top↔bottom turns it into (q + r, -r).
        val topRight = panel.cornerCells.getValue(CornerPosition.TOP_RIGHT)
        val topLeft = panel.cornerCells.getValue(CornerPosition.TOP_LEFT)
        val bottomRight = panel.cornerCells.getValue(CornerPosition.BOTTOM_RIGHT)
        val bottomLeft = panel.cornerCells.getValue(CornerPosition.BOTTOM_LEFT)
        assertEquals(topLeft, topRight.map { HexCoord(-it.q - it.r, it.r) }.toSet())
        assertEquals(bottomRight, topRight.map { HexCoord(it.q + it.r, -it.r) }.toSet())
        assertEquals(bottomLeft, topRight.map { HexCoord(-it.q, -it.r) }.toSet())
    }

    @Test
    fun `corner cells never overlap the panel or each other`() {
        val all = panel.cornerCells.values.flatten()
        assertEquals(all.size, all.toSet().size)
        assertTrue(all.none { it in panel })
    }

    @Test
    fun `every cell knows its owner`() {
        assertEquals(PanelSlot.Cell(HexCoord.ORIGIN), panel.slotOf(HexCoord.ORIGIN))
        assertEquals(PanelSlot.Corner(CornerPosition.TOP_RIGHT), panel.slotOf(HexCoord(4, -1)))
        assertEquals(null, panel.slotOf(HexCoord(0, -3))) // above the panel: nobody's
    }

    // --- Merged hexagons ---

    private val bar = setOf(HexCoord(0, 2), HexCoord(1, 2))
    private val withBar = panel.copy(merged = listOf(bar))

    @Test
    fun `merging 2 hexagons turns them into a single slot`() {
        assertEquals(32, withBar.slots.size)
        assertEquals(1, withBar.slots.count { it == PanelSlot.Merged(bar) })
        assertTrue(withBar.slots.none { it is PanelSlot.Cell && it.coord in bar })
    }

    @Test
    fun `a merged group takes the place of its hexagon nearest to the centre`() {
        val nearest = withBar.cells.indexOfFirst { it in bar }
        assertEquals(nearest, withBar.slots.indexOf(PanelSlot.Merged(bar)))
    }

    @Test
    fun `both merged hexagons belong to the same slot`() {
        bar.forEach { assertEquals(PanelSlot.Merged(bar), withBar.slotOf(it)) }
    }

    @Test
    fun `merging doesn't change the silhouette`() {
        assertEquals(panel.cells, withBar.cells)
    }

    @Test
    fun `invalid merged groups are rejected`() {
        val invalid = listOf(
            setOf(HexCoord.ORIGIN),                                    // only one hexagon
            setOf(HexCoord(3, 0), HexCoord(4, 0)),                     // (4,0) is outside the panel
            setOf(HexCoord.ORIGIN, HexCoord(2, 0)),                    // they don't touch
        )
        invalid.forEach { group ->
            assertThrows(IllegalArgumentException::class.java) { panel.copy(merged = listOf(group)) }
        }
        assertThrows(IllegalArgumentException::class.java) {   // two groups sharing a hexagon
            panel.copy(merged = listOf(bar, setOf(HexCoord(1, 2), HexCoord(2, 1))))
        }
    }

    @Test
    fun `a bigger panel grows bigger corners`() {
        val big = HexPanel(rowsAroundCenter = 3, rings = 4)
        // Rows -1, -2 and -3 each leave cells at the right end.
        assertEquals(setOf(-1, -2, -3), big.cornerCells.getValue(CornerPosition.TOP_RIGHT).map { it.r }.toSet())
    }

    @Test
    fun `contains tells apart the panel's hexagons from the ones that go beyond it`() {
        assertTrue(HexCoord.ORIGIN in panel)
        assertTrue(HexCoord(3, 0) in panel)   // end of the E row
        assertFalse(HexCoord(4, 0) in panel)  // ring 4: too far
        assertFalse(HexCoord(0, 3) in panel)  // row 3: outside the 5 rows
    }
}
