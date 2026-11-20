package pol.rubiano.eyesensekeyboard.hexgrid.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HexSpiralTest {

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
