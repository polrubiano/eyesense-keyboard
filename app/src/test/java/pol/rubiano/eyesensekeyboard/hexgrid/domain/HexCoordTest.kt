package pol.rubiano.eyesensekeyboard.hexgrid.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HexCoordTest {

    @Test
    fun `the three axes always add up to zero`() {
        val coords = listOf(HexCoord(0, 0), HexCoord(2, -1), HexCoord(-3, 5))
        coords.forEach { assertEquals(0, it.q + it.r + it.s) }
    }

    @Test
    fun `the origin has 6 neighbours, all in ring 1`() {
        val neighbors = HexCoord.ORIGIN.neighbors()
        assertEquals(6, neighbors.size)
        assertEquals(6, neighbors.toSet().size)
        assertTrue(neighbors.all { it.ring == 1 })
    }

    @Test
    fun `distance counts steps, not straight-line length`() {
        // Two steps east
        assertEquals(2, HexCoord(0, 0).distanceTo(HexCoord(2, 0)))
        // East + south-east: still 2 steps
        assertEquals(2, HexCoord(0, 0).distanceTo(HexCoord(1, 1)))
        // Distance is symmetric
        val a = HexCoord(-2, 3)
        val b = HexCoord(1, -1)
        assertEquals(a.distanceTo(b), b.distanceTo(a))
    }

    @Test
    fun `going round all 6 directions brings you back to the start`() {
        val start = HexCoord(3, -2)
        val end = HexCoord.DIRECTIONS.fold(start) { acc, dir -> acc + dir }
        assertEquals(start, end)
    }

    @Test
    fun `multiplying a direction moves that many steps in a straight line`() {
        val threeEast = HexCoord.DIRECTIONS[0] * 3
        assertEquals(HexCoord(3, 0), threeEast)
        assertEquals(3, threeEast.ring)
    }

    @Test
    fun `the doubled column counts half hexagons from the middle, rows shifted by half`() {
        assertEquals(0, HexCoord.ORIGIN.doubledColumn)
        assertEquals(2, HexCoord(1, 0).doubledColumn)   // east: one whole hexagon
        assertEquals(1, HexCoord(0, 1).doubledColumn)   // south-east: half a hexagon east
        assertEquals(-1, HexCoord(-1, 1).doubledColumn) // south-west: half a hexagon west
    }

    // --- hexRing / hexSpiral ---

    @Test
    fun `ring 0 is just the centre`() {
        assertEquals(listOf(HexCoord.ORIGIN), hexRing(0))
    }

    @Test
    fun `ring k has 6k distinct cells, all at distance k`() {
        for (k in 1..5) {
            val ring = hexRing(k)
            assertEquals(6 * k, ring.size)
            assertEquals(ring.size, ring.toSet().size)
            assertTrue(ring.all { it.ring == k })
        }
    }

    @Test
    fun `ring 1 is traversed clockwise starting from east`() {
        assertEquals(HexCoord.DIRECTIONS, hexRing(1))
    }

    @Test
    fun `the spiral starts at the centre and never goes back to an earlier ring`() {
        val spiral = hexSpiral().take(61).toList()
        assertEquals(HexCoord.ORIGIN, spiral.first())
        val rings = spiral.map { it.ring }
        assertEquals(rings.sorted(), rings)
        assertEquals(spiral.size, spiral.toSet().size)
    }
}
