package pol.rubiano.eyesensekeyboard.hexgrid.domain

import kotlin.math.abs
import kotlin.math.hypot

/*
 * Operations on polygons. A polygon is a list of vertices; nothing here knows about hexagons.
 *
 * Every polygon here goes **clockwise on screen** (Y grows downward), like hexCorners().
 */

/**
 * A point in pixels. Its own type so the domain doesn't depend on Compose.
 *
 * It also works as the **path between two points**: `to - from` is "how far, and in which
 * direction". That lets formulas read like the idea instead of coordinate by coordinate:
 * ```
 * from + (to - from) * 0.5f      the midpoint: leave `from` and go half the way to `to`
 * ```
 */
data class Point(val x: Float, val y: Float) {

    /** Moves this point along [other] taken as a path. */
    operator fun plus(other: Point): Point = Point(x + other.x, y + other.y)

    /** The path from [other] to this point. */
    operator fun minus(other: Point): Point = Point(x - other.x, y - other.y)

    /** Stretches (factor > 1) or shrinks (factor < 1) this path. */
    operator fun times(factor: Float): Point = Point(x * factor, y * factor)

    /** Straight-line length from this point to [other]. */
    fun distanceTo(other: Point): Float = hypot(other.x - x, other.y - y)
}

/**
 * The element at [i] with its two neighbours, as if the list were a ring: the one before
 * the first is the last, the one after the last is the first. Made for polygons, where
 * every vertex has a previous and a next one.
 *
 * ```
 * val (prev, vertex, next) = polygon.around(i)
 * ```
 */
fun <T> List<T>.around(i: Int): Triple<T, T, T> =
    Triple(this[(i - 1 + size) % size], this[i], this[(i + 1) % size])

/** Below this many pixels, two points are the same point. */
internal const val EPSILON = 1e-3f

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
    return polygon.indices.map { i ->
        val (prev, vertex, next) = polygon.around(i)
        intersect(shifted(prev, vertex, distance), shifted(vertex, next, distance))
    }
}

/** Area of the polygon, by the shoelace formula. Positive when it goes clockwise on screen. */
fun List<Point>.signedArea(): Float =
    indices.sumOf { i ->
        val (_, a, b) = around(i)
        (a cross b).toDouble()
    }.toFloat() / 2f

// --- Helpers ---------------------------------------------------------------------------

/** One scissors cut of Sutherland–Hodgman: keeps the part where [inside] holds. */
private fun List<Point>.clip(inside: (Point) -> Boolean, crossing: (Point, Point) -> Point): List<Point> {
    if (isEmpty()) return this
    val out = mutableListOf<Point>()
    indices.forEach { i ->
        val (previous, current) = around(i)
        if (inside(current)) {
            if (!inside(previous)) out += crossing(previous, current)
            out += current
        } else if (inside(previous)) {
            out += crossing(previous, current)
        }
    }
    return out
}

/**
 * Point of the segment this→[to] at the given x (or y): go the fraction of the way where
 * the segment reaches it. That coordinate is then set exactly, so the cut lies right on
 * the edge despite float rounding.
 */
private fun Point.atX(to: Point, x: Float): Point = (this + (to - this) * ((x - this.x) / (to.x - this.x))).copy(x = x)

private fun Point.atY(to: Point, y: Float): Point = (this + (to - this) * ((y - this.y) / (to.y - this.y))).copy(y = y)

/**
 * Removes repeated vertices and vertices in the middle of a straight line.
 * Neither changes the shape, but both break [insetBy].
 */
private fun List<Point>.simplified(): List<Point> {
    var points = this
    while (true) {
        if (points.size < 3) return points
        val i = points.indices.firstOrNull { i ->
            val (prev, vertex, next) = points.around(i)
            prev.distanceTo(vertex) < EPSILON ||
                abs((vertex - prev) cross (next - vertex)) <
                EPSILON * (prev.distanceTo(vertex) + vertex.distanceTo(next))
        } ?: return points
        points = points.filterIndexed { j, _ -> j != i }
    }
}

/** A straight line: a point and a unit-length [direction]. */
private class Line(val point: Point, val direction: Point)

/**
 * The line through a→b, shifted [distance] inwards. In a clockwise-on-screen polygon the
 * inside is to the right of the direction of travel: the direction turned a quarter
 * clockwise, (-dy, dx).
 */
private fun shifted(a: Point, b: Point, distance: Float): Line {
    val direction = (b - a) * (1f / a.distanceTo(b))
    val inward = Point(-direction.y, direction.x)
    return Line(a + inward * distance, direction)
}

/** Where two non-parallel lines cross. */
private fun intersect(l1: Line, l2: Line): Point {
    val t = ((l2.point - l1.point) cross l2.direction) / (l1.direction cross l2.direction)
    return l1.point + l1.direction * t
}

/**
 * The cross product of two paths: positive when [other] turns clockwise on screen from this
 * one, negative when it turns the other way, zero when they're parallel.
 */
private infix fun Point.cross(other: Point): Float = x * other.y - y * other.x
