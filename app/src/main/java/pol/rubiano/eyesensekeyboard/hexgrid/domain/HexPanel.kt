package pol.rubiano.eyesensekeyboard.hexgrid.domain

import kotlin.math.abs

/**
 * Logical silhouette of the panel: which slots exist. It knows nothing about pixels
 * or about which key goes where.
 *
 * It is built in two steps on top of the spiral:
 * 1. take every cell up to ring [rings] (a large hexagon around the centre);
 * 2. drop the ones that go beyond [rowsAroundCenter] rows above or below.
 *
 * Step 2 is what "flattens" the hexagon: from that ring onwards it only grows sideways.
 * The 4 gaps left between the silhouette and the rectangle become the triangles.
 *
 * Example (EyeSense keyboard): `HexPanel(rowsAroundCenter = 2, rings = 3)`
 * → rows of 5-6-7-6-5 = 29 hexagons + 4 triangles = 33 slots.
 */
data class HexPanel(val rowsAroundCenter: Int, val rings: Int) {

    init {
        require(rowsAroundCenter >= 0) { "rowsAroundCenter can't be negative: $rowsAroundCenter" }
        require(rings >= 0) { "rings can't be negative: $rings" }
    }

    /** Hexagons, from nearest to farthest from the centre. The first one is always the origin. */
    val cells: List<HexCoord> = hexSpiral()
        .takeWhile { it.ring <= rings }
        .filter { abs(it.r) <= rowsAroundCenter }
        .toList()

    val corners: List<PanelSlot.Corner> = CornerPosition.entries.map { PanelSlot.Corner(it) }

    /** Every slot: hexagons first (by closeness to the centre), then the triangles. */
    val slots: List<PanelSlot> = cells.map { PanelSlot.Cell(it) } + corners

    /**
     * Rows that actually have hexagons above/below the centre. If [rings] is smaller than
     * [rowsAroundCenter], the rings don't reach far enough to fill every row.
     */
    val visibleRowsAroundCenter: Int = minOf(rowsAroundCenter, rings)

    private val cellSet: Set<HexCoord> = cells.toSet()

    /** Is this coordinate a hexagon of the panel? */
    operator fun contains(coord: HexCoord): Boolean = coord in cellSet
}
