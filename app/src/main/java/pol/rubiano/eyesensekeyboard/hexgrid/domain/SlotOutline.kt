package pol.rubiano.eyesensekeyboard.hexgrid.domain

import kotlin.math.abs

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
 * - Corner key → its cells joined into one shape, cut to the panel's rectangle.
 */
fun PanelGeometry.outline(slot: PanelSlot): SlotOutline = when (slot) {
    is PanelSlot.Cell -> SlotOutline(
        vertices = hexCorners(slot.coord),
        labelCenter = center(slot.coord),
        labelRadius = hexWidth / 2f, // from the centre to the middle of a side
    )
    is PanelSlot.Corner -> {
        val cells = panel.cornerCells.getValue(slot.position)
        // The cell that sits farthest inside the panel horizontally: the one that fits whole.
        val wholeCell = cells.minBy { abs(2 * it.q + it.r) }
        SlotOutline(
            vertices = clippedToPanel(regionOutline(cells)),
            labelCenter = center(wholeCell),
            labelRadius = hexWidth / 2f,
        )
    }
}
