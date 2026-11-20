package pol.rubiano.eyesensekeyboard.hexgrid.domain

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/*
 * Pixel → slot. It's the inverse of PanelGeometry.center().
 *
 * It works the same for a finger or for an eye-tracker gaze point: both are a point
 * (x, y) in the panel's local coordinates.
 */

/**
 * Which slot is under [point]? Null if the point falls outside the panel.
 *
 * Steps, from fastest to slowest:
 * 1. Outside the rectangle → null.
 * 2. Work out the hexagon mathematically ([pixelToHex]). If it belongs to the panel → done.
 * 3. If not, it's on the edge: check whether it falls inside one of the 4 triangles.
 * 4. If it doesn't hit a triangle either, it's one of the small nooks left by the honeycomb's
 *    zigzag edge → it goes to the nearest hexagon. That way there are no dead zones.
 */
fun PanelGeometry.slotAt(point: Point): PanelSlot? {
    if (point.x !in 0f..width || point.y !in 0f..height) return null

    val coord = pixelToHex(point)
    if (coord in panel) return PanelSlot.Cell(coord)

    CornerPosition.entries
        .firstOrNull { isInsideTriangle(point, triangle(it)) }
        ?.let { return PanelSlot.Corner(it) }

    val nearest = panel.cells.minBy { cell ->
        val c = center(cell)
        (c.x - point.x) * (c.x - point.x) + (c.y - point.y) * (c.y - point.y)
    }
    return PanelSlot.Cell(nearest)
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

/** Is the point inside the triangle? It is if it's on the same side of all three edges. */
private fun isInsideTriangle(p: Point, triangle: List<Point>): Boolean {
    val (a, b, c) = triangle
    fun side(from: Point, to: Point) = (to.x - from.x) * (p.y - from.y) - (to.y - from.y) * (p.x - from.x)
    val s1 = side(a, b)
    val s2 = side(b, c)
    val s3 = side(c, a)
    val hasNegative = s1 < 0 || s2 < 0 || s3 < 0
    val hasPositive = s1 > 0 || s2 > 0 || s3 > 0
    return !(hasNegative && hasPositive)
}
