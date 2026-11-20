package pol.rubiano.eyesensekeyboard.hexgrid.domain

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** A point in pixels. Its own type so the domain doesn't depend on Compose. */
data class Point(val x: Float, val y: Float)

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

    /**
     * The 3 vertices of a triangle: [corner of the panel, point on the top/bottom edge,
     * point aimed at the centre on the side edge].
     *
     * Its long side is the line that joins the outer vertices of the hexagons at the ends
     * of each row: it touches them without overlapping and leaves no gaps.
     * It is computed for [CornerPosition.TOP_LEFT] and the other three are its mirror images.
     */
    fun triangle(position: CornerPosition): List<Point> {
        // The point sits at the height of the top-left vertex of the first hexagon in
        // the central row: half a radius above the centre.
        val tipY = height / 2f - hexRadius / 2f
        // From there the long side climbs at 60°: half a hexagon sideways for every row
        // it climbs. Up to the top edge it climbs (rows + 1/3) rows.
        val topLegLength = (rows + 1f / 3f) * hexWidth / 2f

        val topLeft = listOf(Point(0f, 0f), Point(topLegLength, 0f), Point(0f, tipY))
        return topLeft.map { p ->
            Point(
                x = if (position.isLeft) p.x else width - p.x,
                y = if (position.isTop) p.y else height - p.y,
            )
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
