package pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid

import kotlin.math.abs
import kotlin.math.roundToLong

/*
 * Outlines of shapes made of several cells: a key that spans more than one hexagon.
 *
 * Three steps, each a pure function on a list of vertices (a polygon):
 *   ① regionOutline   join the cells into one outline
 *   ② clippedToPanel  cut away whatever sticks out of the panel
 *   ③ insetBy         move every side inwards (the visual gap between keys)
 *
 * Every polygon here goes **clockwise on screen** (Y grows downward), like hexCorners().
 */

/**
 * Outline of a slot in pixels, plus the circle where its label goes.
 *
 * The label circle is always the inscribed circle of **one hexagon**: the slot's own one,
 * or, for a corner key, the cell that fits whole inside the panel (▣). That way icons sit
 * in a hexagonal area, lined up with the letters of their row and just as big.
 */
data class SlotOutline(
    val vertices: List<Point>,
    val labelCenter: Point,
    val labelRadius: Float,
) {
    /**
     * The same shape with every side moved [distance] px inwards ([insetBy]).
     * Used to draw the visual gap between keys.
     */
    fun inset(distance: Float): SlotOutline {
        if (distance <= 0f) return this
        require(distance < labelRadius) { "The inset ($distance) doesn't fit in the shape (label radius $labelRadius)" }
        return SlotOutline(
            vertices = vertices.insetBy(distance),
            labelCenter = labelCenter,
            labelRadius = labelRadius - distance,
        )
    }
}

/**
 * Pixel outline of any slot.
 * - Hexagon → its 6 corners.
 * - Merged hexagons → their cells joined into one shape, labelled at the middle of
 *   their centres (with 2 cells, where the wall used to be; with 3 in a row, on the middle one).
 * - Corner key → its cells joined into one shape, cut to the panel's rectangle.
 */
fun PanelGeometry.outline(slot: PanelSlot): SlotOutline = when (slot) {
    is PanelSlot.Cell -> SlotOutline(
        vertices = hexCorners(slot.coord),
        labelCenter = center(slot.coord),
        labelRadius = inradius,
    )
    is PanelSlot.Merged -> {
        val centers = slot.cells.map { center(it) }
        SlotOutline(
            vertices = regionOutline(slot.cells),
            labelCenter = Point(centers.map { it.x }.average().toFloat(), centers.map { it.y }.average().toFloat()),
            labelRadius = inradius,
        )
    }
    is PanelSlot.Corner -> {
        val cells = panel.cornerCells.getValue(slot.position)
        // The cell that sits farthest inside the panel horizontally: the one that fits whole.
        val wholeCell = cells.minBy { abs(it.doubledColumn) }
        SlotOutline(
            vertices = clippedToPanel(regionOutline(cells)),
            labelCenter = center(wholeCell),
            labelRadius = inradius,
        )
    }
}

/**
 * ① The outline of a group of cells, as if they were a single piece.
 *
 * Each hexagon has 6 sides. A side stays only if the cell on the other side is NOT in the
 * group; otherwise it's an inner wall and goes away. Side `i` (from vertex `i` to `i + 1`)
 * faces exactly [HexCoord.DIRECTIONS]`[i]`, so the check uses whole-number coordinates only.
 *
 * The remaining sides are then chained: where one ends, the next one begins.
 *
 * The group must be a single piece with no holes: a key with a hole in it makes no sense.
 */
fun PanelGeometry.regionOutline(cells: Set<HexCoord>): List<Point> {
    require(cells.isNotEmpty()) { "A region needs at least one cell" }

    // Outer sides, indexed by where they start.
    val sides = HashMap<Long, Pair<Point, Point>>()
    cells.forEach { cell ->
        val corners = hexCorners(cell)
        HexCoord.DIRECTIONS.forEachIndexed { i, direction ->
            if (cell + direction !in cells) {
                val start = corners[i]
                sides[keyOf(start)] = start to corners[(i + 1) % 6]
            }
        }
    }

    // Walk round from any side until we're back at the start.
    val outline = mutableListOf<Point>()
    val start = sides.values.first()
    var side = start
    do {
        outline += side.first
        side = sides[keyOf(side.second)] ?: error("Broken outline: $cells")
    } while (side !== start)

    // A single loop must use every outer side. If some are left over, there's a second
    // loop: another piece, or the edge of a hole.
    require(outline.size == sides.size) { "The cells must form a single piece with no holes: $cells" }
    return outline
}

// --- Helpers ---------------------------------------------------------------------------

/** Points that are the same within [EPSILON] get the same key, despite float noise. */
private fun keyOf(p: Point): Long {
    val x = (p.x / EPSILON).roundToLong()
    val y = (p.y / EPSILON).roundToLong()
    return x * 1_000_003L + y
}
