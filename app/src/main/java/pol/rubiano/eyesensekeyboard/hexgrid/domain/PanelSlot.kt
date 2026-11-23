package pol.rubiano.eyesensekeyboard.hexgrid.domain

/**
 * A slot in the panel where a key can go. It says *where*, not *what*.
 *
 * Three kinds: a single hexagon of the panel, several hexagons merged into one
 * ([HexPanel.merged]), or a corner key made of the cells that fill one of the 4 gaps
 * beside the honeycomb ([HexPanel.cornerCells]).
 */
sealed interface PanelSlot {

    /** A hexagon of the honeycomb, identified by its coordinate. */
    data class Cell(val coord: HexCoord) : PanelSlot

    /** Several hexagons of the honeycomb that act as one (e.g. a space bar). */
    data class Merged(val cells: Set<HexCoord>) : PanelSlot

    /** One of the 4 corner keys that close the honeycomb from the sides. */
    data class Corner(val position: CornerPosition) : PanelSlot
}

/**
 * The 4 corner keys. Each one has its straight side against the left/right edge and
 * reaches down (or up) to the central row.
 */
enum class CornerPosition(val isTop: Boolean, val isLeft: Boolean) {
    TOP_LEFT(isTop = true, isLeft = true),
    BOTTOM_LEFT(isTop = false, isLeft = true),
    TOP_RIGHT(isTop = true, isLeft = false),
    BOTTOM_RIGHT(isTop = false, isLeft = false),
}
