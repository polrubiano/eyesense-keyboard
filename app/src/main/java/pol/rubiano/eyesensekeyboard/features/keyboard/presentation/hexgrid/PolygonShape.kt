package pol.rubiano.eyesensekeyboard.features.keyboard.presentation.hexgrid

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.Point
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.around
import kotlin.math.min

/**
 * Compose [Shape] for any polygon, with its corners optionally rounded.
 *
 * The vertices are **normalised** (0..1 within the element's box), so the shape scales
 * with the element. It's what lets you use `background(color, shape)` or
 * `border(..., shape)` with hexagons and corner keys just like with `CircleShape`.
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
    vertices.indices.forEach { i ->
        val (prev, v, next) = vertices.around(i)
        val cut = min(radius, min(v.distanceTo(prev), v.distanceTo(next)) / 2f)
        val a = towards(v, prev, cut)
        val b = towards(v, next, cut)
        if (i == 0) moveTo(a.x, a.y) else lineTo(a.x, a.y)
        quadraticTo(v.x, v.y, b.x, b.y)
    }
    close()
}

/** The point [length] px away from [from], heading to [to]: a fraction of the way there. */
private fun towards(from: Point, to: Point, length: Float): Point =
    from + (to - from) * (length / from.distanceTo(to))
