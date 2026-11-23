package pol.rubiano.eyesensekeyboard.hexgrid.domain

import kotlin.math.abs
import kotlin.math.hypot
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

/** Below this many pixels, two points are the same point. */
private const val EPSILON = 1e-3f

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

/**
 * ② The polygon cut to the panel's rectangle (0..[PanelGeometry.width] × 0..[PanelGeometry.height]).
 *
 * Sutherland–Hodgman: cut against each of the 4 edges, one after the other, like going
 * round the rectangle with scissors. Against each edge:
 * - a vertex inside stays;
 * - a side that crosses the edge adds the crossing point;
 * - a vertex outside goes.
 */
fun PanelGeometry.clippedToPanel(polygon: List<Point>): List<Point> {
    var result = polygon
    result = result.clip({ it.x >= -EPSILON }) { a, b -> a.atX(b, 0f) }
    result = result.clip({ it.x <= width + EPSILON }) { a, b -> a.atX(b, width) }
    result = result.clip({ it.y >= -EPSILON }) { a, b -> a.atY(b, 0f) }
    result = result.clip({ it.y <= height + EPSILON }) { a, b -> a.atY(b, height) }
    return result.simplified()
}

/**
 * ③ The same polygon with every side moved [distance] px inwards.
 *
 * 1. Each side shifts inwards, parallel to itself.
 * 2. Each new vertex is where its two shifted sides cross.
 *
 * Works for outward corners and inward nooks alike. [distance] must be small compared to
 * the sides (a few px of gap): with a huge one a short side would vanish.
 */
fun List<Point>.insetBy(distance: Float): List<Point> {
    if (distance <= 0f) return this
    // Straight-line vertices have parallel sides that never cross: remove them first.
    val polygon = simplified()
    val n = polygon.size
    return polygon.indices.map { i ->
        val prev = polygon[(i - 1 + n) % n]
        val v = polygon[i]
        val next = polygon[(i + 1) % n]
        intersect(shifted(prev, v, distance), shifted(v, next, distance))
    }
}

/** Area of the polygon, by the shoelace formula. Positive when it goes clockwise on screen. */
fun List<Point>.signedArea(): Float =
    indices.sumOf { i ->
        val a = this[i]
        val b = this[(i + 1) % size]
        (a.x * b.y - b.x * a.y).toDouble()
    }.toFloat() / 2f

// --- Helpers ---------------------------------------------------------------------------

/** Points that are the same within [EPSILON] get the same key, despite float noise. */
private fun keyOf(p: Point): Long {
    val x = (p.x / EPSILON).roundToLong()
    val y = (p.y / EPSILON).roundToLong()
    return x * 1_000_003L + y
}

/** One scissors cut of Sutherland–Hodgman: keeps the part where [inside] holds. */
private fun List<Point>.clip(inside: (Point) -> Boolean, crossing: (Point, Point) -> Point): List<Point> {
    if (isEmpty()) return this
    val out = mutableListOf<Point>()
    forEachIndexed { i, current ->
        val previous = this[(i - 1 + size) % size]
        if (inside(current)) {
            if (!inside(previous)) out += crossing(previous, current)
            out += current
        } else if (inside(previous)) {
            out += crossing(previous, current)
        }
    }
    return out
}

/** Point of the segment this→[to] at the given x (or y). */
private fun Point.atX(to: Point, x: Float): Point = Point(x, y + (to.y - y) * (x - this.x) / (to.x - this.x))

private fun Point.atY(to: Point, y: Float): Point = Point(x + (to.x - x) * (y - this.y) / (to.y - this.y), y)

/**
 * Removes repeated vertices and vertices in the middle of a straight line.
 * Neither changes the shape, but both break [insetBy].
 */
private fun List<Point>.simplified(): List<Point> {
    var points = this
    while (true) {
        val n = points.size
        if (n < 3) return points
        val i = points.indices.firstOrNull { i ->
            val prev = points[(i - 1 + n) % n]
            val v = points[i]
            val next = points[(i + 1) % n]
            distance(prev, v) < EPSILON ||
                abs(cross(v.x - prev.x, v.y - prev.y, next.x - v.x, next.y - v.y)) <
                EPSILON * (distance(prev, v) + distance(v, next))
        } ?: return points
        points = points.filterIndexed { j, _ -> j != i }
    }
}

/** A straight line: a point and a unit direction. */
private class Line(val point: Point, val dx: Float, val dy: Float)

/**
 * The line through a→b, shifted [distance] inwards. In a clockwise-on-screen polygon the
 * inside is to the right of the direction of travel: normal = (-dy, dx).
 */
private fun shifted(a: Point, b: Point, distance: Float): Line {
    val length = distance(a, b)
    val dx = (b.x - a.x) / length
    val dy = (b.y - a.y) / length
    return Line(Point(a.x - dy * distance, a.y + dx * distance), dx, dy)
}

/** Where two non-parallel lines cross. */
private fun intersect(l1: Line, l2: Line): Point {
    val t = cross(l2.point.x - l1.point.x, l2.point.y - l1.point.y, l2.dx, l2.dy) / cross(l1.dx, l1.dy, l2.dx, l2.dy)
    return Point(l1.point.x + l1.dx * t, l1.point.y + l1.dy * t)
}

private fun cross(ax: Float, ay: Float, bx: Float, by: Float): Float = ax * by - ay * bx

private fun distance(p: Point, q: Point): Float = hypot(q.x - p.x, q.y - p.y)
