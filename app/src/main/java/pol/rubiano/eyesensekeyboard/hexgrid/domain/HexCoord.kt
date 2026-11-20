package pol.rubiano.eyesensekeyboard.hexgrid.domain

import kotlin.math.abs
import kotlin.math.max

/**
 * Position of a cell in a hexagonal grid, using **axial coordinates** (q, r).
 *
 * Orientation: pointy-top hexagons (vertex up), with rows laid out horizontally.
 * Screen convention: `r` grows downward, like the Y axis in Compose.
 *
 * ```
 *              (0,-1)   (1,-1)
 *         (-1,0)   (0,0)    (1,0)
 *             (-1,1)    (0,1)
 * ```
 *
 * - `q` → moving along a row (left/right).
 * - `r` → which row you're on (up/down). Moving one row shifts the row by half a hexagon.
 * - `s` → the hidden third axis. It is derived as `-q - r`, so q + r + s == 0 always holds.
 *
 * The coordinate is the **identity** of a cell. What it holds (a letter, an icon, an empty
 * slot...) lives outside this class.
 */
data class HexCoord(val q: Int, val r: Int) {

    /** Third axis, derived. It isn't stored so it can never be inconsistent. */
    val s: Int get() = -q - r

    operator fun plus(other: HexCoord): HexCoord = HexCoord(q + other.q, r + other.r)

    operator fun minus(other: HexCoord): HexCoord = HexCoord(q - other.q, r - other.r)

    /** Repeats a displacement: `DIRECTIONS[0] * 3` = three steps east. */
    operator fun times(factor: Int): HexCoord = HexCoord(q * factor, r * factor)

    /** Number of hexagon-to-hexagon steps needed to get from this cell to [other]. */
    fun distanceTo(other: HexCoord): Int {
        val d = this - other
        return max(abs(d.q), max(abs(d.r), abs(d.s)))
    }

    /** Ring this cell belongs to, counting from the origin (0 = the centre itself). */
    val ring: Int get() = distanceTo(ORIGIN)

    /** The 6 adjacent cells, in the same order as [DIRECTIONS]. */
    fun neighbors(): List<HexCoord> = DIRECTIONS.map { this + it }

    companion object {
        /** The optical centre: the cell of the most frequent key. */
        val ORIGIN = HexCoord(0, 0)

        /**
         * The 6 unit steps, clockwise starting from east.
         * Their order matters: it decides how rings are traversed later on.
         */
        val DIRECTIONS: List<HexCoord> = listOf(
            HexCoord(1, 0),   // east
            HexCoord(0, 1),   // south-east
            HexCoord(-1, 1),  // south-west
            HexCoord(-1, 0),  // west
            HexCoord(0, -1),  // north-west
            HexCoord(1, -1),  // north-east
        )
    }
}
