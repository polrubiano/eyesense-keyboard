package pol.rubiano.eyesensekeyboard.hexgrid.presentation

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import pol.rubiano.eyesensekeyboard.hexgrid.domain.Point
import kotlin.math.hypot
import kotlin.math.min

/**
 * Compose [Shape] for any polygon, with its corners optionally rounded.
 *
 * The vertices are **normalised** (0..1 within the element's box), so the shape scales
 * with the element. It's what lets you use `background(color, shape)` or
 * `border(..., shape)` with hexagons and triangles just like with `CircleShape`.
 *
 * [cornerRadius] is in **pixels**, not normalised: a corner must look equally round
 * whatever the box's proportions.
 */
data class PolygonShape(
    private val normalizedVertices: List<Point>,
    private val cornerRadius: Float = 0f,
) : Shape {

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val vertices = normalizedVertices.map { Point(it.x * size.width, it.y * size.height) }
        return Outline.Generic(roundedPolygonPath(vertices, cornerRadius))
    }
}

/**
 * Path of a polygon with every vertex rounded.
 *
 * At each vertex V, between its neighbours P and N:
 * ```
 *        V                      V (no longer drawn)
 *       / \                    ·  ·
 *      /   \       →        A ╭────╮ B     A and B: [radius] back from V along each side
 *     /     \                /      \      line to A, then a curve from A to B
 *    P       N              P        N     with V as the control point ("magnet")
 * ```
 * The cut never exceeds half of either side, so two curves on the same side never overlap,
 * however big the radius.
 */
private fun roundedPolygonPath(vertices: List<Point>, radius: Float): Path = Path().apply {
    if (radius <= 0f) {
        vertices.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
        close()
        return@apply
    }
    val n = vertices.size
    vertices.forEachIndexed { i, v ->
        val prev = vertices[(i - 1 + n) % n]
        val next = vertices[(i + 1) % n]
        val cut = min(radius, min(distance(v, prev), distance(v, next)) / 2f)
        val a = towards(v, prev, cut)
        val b = towards(v, next, cut)
        if (i == 0) moveTo(a.x, a.y) else lineTo(a.x, a.y)
        quadraticTo(v.x, v.y, b.x, b.y)
    }
    close()
}

/** The point [length] px away from [from], heading to [to]. */
private fun towards(from: Point, to: Point, length: Float): Point {
    val d = distance(from, to)
    return Point(from.x + (to.x - from.x) * length / d, from.y + (to.y - from.y) * length / d)
}

private fun distance(p: Point, q: Point): Float = hypot(q.x - p.x, q.y - p.y)
