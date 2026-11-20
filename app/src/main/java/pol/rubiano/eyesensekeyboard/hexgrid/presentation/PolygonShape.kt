package pol.rubiano.eyesensekeyboard.hexgrid.presentation

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import pol.rubiano.eyesensekeyboard.hexgrid.domain.Point

/**
 * Compose [Shape] for any polygon.
 *
 * The vertices are **normalised** (0..1 within the element's box), so the shape scales
 * with the element. It's what lets you use `background(color, shape)` or
 * `border(..., shape)` with hexagons and triangles just like with `CircleShape`.
 */
data class PolygonShape(private val normalizedVertices: List<Point>) : Shape {

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            normalizedVertices.forEachIndexed { index, point ->
                val x = point.x * size.width
                val y = point.y * size.height
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        return Outline.Generic(path)
    }
}
