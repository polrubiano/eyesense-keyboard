package pol.rubiano.eyesensekeyboard.hexgrid.domain

import kotlin.math.abs
import kotlin.math.hypot

/**
 * Outline of a slot in pixels, plus its **inscribed circle**: the largest circle that
 * fits inside it touching every side.
 *
 * Hexagons and triangles both have one. That gives us two things for free:
 * - [incenter] is the natural visual centre for placing the label (in a triangle it is
 *   NOT the centre of its bounding box).
 * - [inset] can shrink the shape evenly: scaling around the incenter moves every side
 *   inwards by the same distance.
 */
data class SlotOutline(
    val vertices: List<Point>,
    val incenter: Point,
    val inradius: Float,
) {
    /**
     * The same shape with every side moved [distance] px inwards.
     * Used to draw the visual gap between keys.
     */
    fun inset(distance: Float): SlotOutline {
        if (distance <= 0f) return this
        require(distance < inradius) { "The inset ($distance) doesn't fit in the shape (inradius $inradius)" }
        val factor = (inradius - distance) / inradius
        return SlotOutline(
            vertices = vertices.map { v ->
                Point(
                    x = incenter.x + (v.x - incenter.x) * factor,
                    y = incenter.y + (v.y - incenter.y) * factor,
                )
            },
            incenter = incenter,
            inradius = inradius - distance,
        )
    }
}

/** Pixel outline of any slot, whether hexagon or triangle. */
fun PanelGeometry.outline(slot: PanelSlot): SlotOutline = when (slot) {
    is PanelSlot.Cell -> SlotOutline(
        vertices = hexCorners(slot.coord),
        incenter = center(slot.coord),
        inradius = hexWidth / 2f, // from the centre to the middle of a side
    )
    is PanelSlot.Corner -> triangleOutline(triangle(slot.position))
}

/**
 * Inscribed circle of a triangle:
 * - centre = average of the vertices weighted by the length of the opposite side;
 * - radius = 2 × area ÷ perimeter.
 */
private fun triangleOutline(vertices: List<Point>): SlotOutline {
    val (a, b, c) = vertices
    val sideA = distance(b, c) // opposite a
    val sideB = distance(a, c) // opposite b
    val sideC = distance(a, b) // opposite c
    val perimeter = sideA + sideB + sideC
    val area = abs((b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y)) / 2f
    return SlotOutline(
        vertices = vertices,
        incenter = Point(
            x = (sideA * a.x + sideB * b.x + sideC * c.x) / perimeter,
            y = (sideA * a.y + sideB * b.y + sideC * c.y) / perimeter,
        ),
        inradius = 2f * area / perimeter,
    )
}

private fun distance(p: Point, q: Point): Float = hypot(q.x - p.x, q.y - p.y)
