package pol.rubiano.eyesensekeyboard.hexgrid.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Translates the logical panel ([HexPanel]) into pixels, given a hexagon size.
 *
 * Every coordinate is **local to the panel**: (0,0) is its top-left corner and
 * ([width], [height]) its bottom-right one. Where the panel sits on screen is the
 * UI's decision.
 *
 * The shapes described here **tile the panel perfectly, with no gaps**: they are the
 * touch zones. The visual separation between keys is applied when drawing.
 *
 * Pointy-top hexagon of radius R (centre to vertex):
 * ```
 *        /\          height   = 2R
 *       /  \         width    = √3·R
 *      |    |        between rows (vertical) = 1.5R  ← rows nest into each other
 *      |    |
 *       \  /
 *        \/
 * ```
 */
class PanelGeometry(val panel: HexPanel, val hexRadius: Float) {

    init {
        require(hexRadius > 0f) { "The hexagon radius must be positive: $hexRadius" }
    }

    private val rows = panel.visibleRowsAroundCenter

    /** Width of a hexagon, and horizontal distance between neighbours in the same row. */
    val hexWidth: Float = SQRT3 * hexRadius

    /** Vertical distance between the centres of two consecutive rows. */
    val rowHeight: Float = 1.5f * hexRadius

    /** The central row is the widest: (2·rings + 1) hexagons. */
    val width: Float = (2 * panel.rings + 1) * hexWidth

    /** Distance between the top and bottom row centres, plus half a hexagon at each end. */
    val height: Float = (3 * rows + 2) * hexRadius

    /** Centre of the origin hexagon. The panel is symmetric, so it sits right in the middle. */
    val origin: Point = Point(width / 2f, height / 2f)

    /**
     * Centre of a hexagon in pixels.
     * - Each step in `q` moves one hexagon width along the row.
     * - Each step in `r` moves down one row **and** half a hexagon sideways (the row offset).
     */
    fun center(coord: HexCoord): Point = Point(
        x = origin.x + hexWidth * (coord.q + coord.r / 2f),
        y = origin.y + rowHeight * coord.r,
    )

    /**
     * The 6 vertices of the hexagon, clockwise starting from the top-right one.
     * Indices: 0 top-right, 1 bottom-right, 2 bottom, 3 bottom-left, 4 top-left, 5 top.
     */
    fun hexCorners(coord: HexCoord): List<Point> {
        val c = center(coord)
        return VERTEX_ANGLES.map { angle ->
            Point(c.x + hexRadius * cos(angle), c.y + hexRadius * sin(angle))
        }
    }

    companion object {
        private val SQRT3 = sqrt(3f)

        /** Vertex angles of a pointy-top hexagon: -30°, 30°, 90°... (Y grows downward). */
        private val VERTEX_ANGLES: List<Float> =
            (0 until 6).map { i -> ((60 * i - 30) * PI / 180.0).toFloat() }

        /**
         * The largest geometry that fits in the available space without distorting the
         * hexagons. Whichever dimension is tighter (width or height) wins; the other one has room to spare.
         */
        fun fit(panel: HexPanel, availableWidth: Float, availableHeight: Float): PanelGeometry {
            val radiusByWidth = availableWidth / ((2 * panel.rings + 1) * SQRT3)
            val radiusByHeight = availableHeight / (3 * panel.visibleRowsAroundCenter + 2)
            return PanelGeometry(panel, min(radiusByWidth, radiusByHeight))
        }
    }
}

/*
 * Pixel → slot. It's the inverse of PanelGeometry.center().
 *
 * It works the same for a finger or for an eye-tracker gaze point: both are a point
 * (x, y) in the panel's local coordinates.
 */

/**
 * Which slot is under [point]? Null if the point falls outside the panel.
 *
 * Point → cell → owner:
 * 1. Outside the rectangle → null.
 * 2. Work out the cell mathematically ([pixelToHex]) and ask the panel whose it is:
 *    a hexagon's or a corner key's ([HexPanel.slotOf]).
 * 3. If it has no owner, it's one of the small nooks left by the zigzag of the top and
 *    bottom edges → it goes to the nearest owned cell. That way there are no dead zones.
 */
fun PanelGeometry.slotAt(point: Point): PanelSlot? {
    if (point.x !in 0f..width || point.y !in 0f..height) return null

    panel.slotOf(pixelToHex(point))?.let { return it }

    val nearest = panel.ownedCells.minBy { center(it).distanceTo(point) }
    return panel.slotOf(nearest)
}

/**
 * Coordinate of the infinite honeycomb that contains [point], even if it isn't part of the panel.
 *
 * 1. Undo the center() formula → we get "decimal" coordinates (e.g. q = 1.3, r = -0.6).
 * 2. Round them to the actual hexagon with [hexRound].
 */
fun PanelGeometry.pixelToHex(point: Point): HexCoord {
    val x = point.x - origin.x
    val y = point.y - origin.y
    // center() did: y = 1.5R·r  and  x = √3·R·(q + r/2). Solving for r and q:
    val r = (2f / 3f) * y / hexRadius
    val q = (sqrt(3f) / 3f * x - y / 3f) / hexRadius
    return hexRound(q, r)
}

/**
 * Rounds decimal coordinates to the hexagon that contains them.
 *
 * Rounding q and r separately is NOT enough: near the edges you can land on a cell that
 * doesn't contain the point. The trick is to use the three axes (q, r, s):
 * - round all three;
 * - the one that moved the most when rounded is the least trustworthy → recompute it from
 *   the other two, so that q + r + s = 0 still holds.
 */
fun hexRound(q: Float, r: Float): HexCoord {
    val s = -q - r
    var rq = q.roundToInt()
    var rr = r.roundToInt()
    val rs = s.roundToInt()

    val dq = abs(rq - q)
    val dr = abs(rr - r)
    val ds = abs(rs - s)

    if (dq > dr && dq > ds) {
        rq = -rr - rs
    } else if (dr > ds) {
        rr = -rq - rs
    }
    // If s moved the most, we don't store it: q and r are already correct.
    return HexCoord(rq, rr)
}
