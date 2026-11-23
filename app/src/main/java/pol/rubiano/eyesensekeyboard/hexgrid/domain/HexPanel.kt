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
 * The 4 gaps left between the silhouette and the rectangle become the corner keys,
 * made of the honeycomb cells that fill them ([cornerCells]).
 *
 * Example (EyeSense keyboard): `HexPanel(rowsAroundCenter = 2, rings = 3)`
 * → rows of 5-6-7-6-5 = 29 hexagons + 4 corner keys = 33 slots.
 *
 * [merged] joins groups of hexagons into single slots ([PanelSlot.Merged]), such as a
 * space bar. The panel doesn't know what they're for: whoever uses it decides.
 * Each group must be inside the panel, be a single piece, and not overlap another group.
 */
data class HexPanel(
    val rowsAroundCenter: Int,
    val rings: Int,
    val merged: List<Set<HexCoord>> = emptyList(),
) {

    init {
        require(rowsAroundCenter >= 0) { "rowsAroundCenter can't be negative: $rowsAroundCenter" }
        require(rings >= 0) { "rings can't be negative: $rings" }
    }

    /** Hexagons, from nearest to farthest from the centre. The first one is always the origin. */
    val cells: List<HexCoord> = hexSpiral()
        .takeWhile { it.ring <= rings }
        .filter { abs(it.r) <= rowsAroundCenter }
        .toList()

    private val cellSet: Set<HexCoord> = cells.toSet()

    init {
        merged.forEach { group ->
            require(group.size >= 2) { "A merged group needs at least 2 hexagons: $group" }
            val outside = group.filterNot { it in cellSet }
            require(outside.isEmpty()) { "Merged hexagons outside the panel: $outside" }
            require(group.isSinglePiece()) { "A merged group must be a single piece: $group" }
        }
        val all = merged.flatten()
        require(all.size == all.toSet().size) { "Merged groups can't overlap: $merged" }
    }

    /** Which merged group each hexagon belongs to, if any. */
    private val groupOf: Map<HexCoord, Set<HexCoord>> =
        merged.flatMap { group -> group.map { it to group } }.toMap()

    val corners: List<PanelSlot.Corner> = CornerPosition.entries.map { PanelSlot.Corner(it) }

    /**
     * Every slot: hexagons first (by closeness to the centre), then the corner keys.
     * A merged group takes the place of its hexagon nearest to the centre.
     */
    val slots: List<PanelSlot> = cells.mapNotNull { coord ->
        val group = groupOf[coord]
        when {
            group == null -> PanelSlot.Cell(coord)
            coord == cells.first { it in group } -> PanelSlot.Merged(group)
            else -> null // already represented by its group
        }
    } + corners

    /**
     * Rows that actually have hexagons above/below the centre. If [rings] is smaller than
     * [rowsAroundCenter], the rings don't reach far enough to fill every row.
     */
    val visibleRowsAroundCenter: Int = minOf(rowsAroundCenter, rings)

    /**
     * The cells that make up each corner key: the ones that are **not** in the panel,
     * sit in a row above or below the central one, and **peek into the rectangle**,
     * even if only halfway.
     *
     * ```
     * row -2     ⬡ ⬡ ⬡ ⬡ ⬡ ▣│     TOP_RIGHT = { (4,-2), (4,-1) }
     * row -1    ⬡ ⬡ ⬡ ⬡ ⬡ ⬡ ◧│     ▣ fits whole · ◧ is cut in half by the edge
     * row  0   ⬡ ⬡ ⬡ ⬡ ⬡ ⬡ ⬡│     the central row is full: the corner ends here
     * ```
     *
     * "Peeking in", in whole numbers: a cell's centre is `q + r/2` hexagon widths from the
     * middle, and the rectangle reaches `rings + 1/2`. Doubling both:
     * `|2q + r| ≤ 2·rings + 1`. The sign of `2q + r` says left or right.
     */
    val cornerCells: Map<CornerPosition, Set<HexCoord>> = CornerPosition.entries.associateWith { position ->
        val limit = 2 * rings + 1
        (1..visibleRowsAroundCenter).flatMap { distance ->
            val r = if (position.isTop) -distance else distance
            (-limit..limit)
                .map { q -> HexCoord(q, r) }
                .filter { it !in cellSet && abs(2 * it.q + it.r) <= limit }
                .filter { (2 * it.q + it.r < 0) == position.isLeft }
        }.toSet()
    }

    /**
     * Which slot each cell belongs to: a hexagon to itself (or to its merged group),
     * a corner cell to its corner.
     */
    private val owners: Map<HexCoord, PanelSlot> =
        cells.associateWith { coord -> groupOf[coord]?.let { PanelSlot.Merged(it) } ?: PanelSlot.Cell(coord) } +
            cornerCells.flatMap { (position, coords) -> coords.map { it to PanelSlot.Corner(position) } }

    /** Every cell that belongs to some slot. */
    val ownedCells: Set<HexCoord> get() = owners.keys

    /** The slot [coord] belongs to, or null if it belongs to none (outside the panel). */
    fun slotOf(coord: HexCoord): PanelSlot? = owners[coord]

    /** Is this coordinate a hexagon of the panel? */
    operator fun contains(coord: HexCoord): Boolean = coord in cellSet
}

/** Can every cell be reached from any other one, stepping only between neighbours of the group? */
private fun Set<HexCoord>.isSinglePiece(): Boolean {
    val reached = mutableSetOf(first())
    val pending = ArrayDeque(reached)
    while (pending.isNotEmpty()) {
        pending.removeFirst().neighbors()
            .filter { it in this && reached.add(it) }
            .forEach { pending.addLast(it) }
    }
    return reached.size == size
}
