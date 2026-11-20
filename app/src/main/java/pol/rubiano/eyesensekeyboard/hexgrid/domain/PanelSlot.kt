package pol.rubiano.eyesensekeyboard.hexgrid.domain

/**
 * A slot in the panel where a key can go. It says *where*, not *what*.
 *
 * There are two kinds, because the triangles don't belong to the honeycomb and so
 * have no hexagonal coordinate.
 */
sealed interface PanelSlot {

    /** A hexagon of the honeycomb, identified by its coordinate. */
    data class Cell(val coord: HexCoord) : PanelSlot

    /** One of the 4 triangles that hug the honeycomb from the sides. */
    data class Corner(val position: CornerPosition) : PanelSlot
}

/**
 * The 4 triangles. Each one has its vertical side against the left/right edge and its
 * point aimed at the central row.
 */
enum class CornerPosition(val isTop: Boolean, val isLeft: Boolean) {
    TOP_LEFT(isTop = true, isLeft = true),
    BOTTOM_LEFT(isTop = false, isLeft = true),
    TOP_RIGHT(isTop = true, isLeft = false),
    BOTTOM_RIGHT(isTop = false, isLeft = false),
}
