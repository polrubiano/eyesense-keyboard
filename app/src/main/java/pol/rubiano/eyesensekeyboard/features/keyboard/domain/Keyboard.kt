package pol.rubiano.eyesensekeyboard.features.keyboard.domain

import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexPanel
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.PanelSlot

/**
 * The keyboard in use: a panel silhouette + a character layout + a function layout.
 *
 * The two layouts are independent: changing language ([withCharacters]) keeps the
 * functions where they were.
 */
data class Keyboard(
    private val silhouette: HexPanel,
    val characters: CharacterLayout,
    val functions: FunctionLayout,
) {
    init {
        val outside = characters.cells.keys.filterNot { it in silhouette }
        require(outside.isEmpty()) {
            "The layout \"${characters.name}\" has keys outside the panel: $outside"
        }
    }

    /**
     * The panel to draw: the silhouette with the character layout's merged keys
     * (e.g. the space bar). It changes with the language, because each language has
     * its own space bar.
     */
    val panel: HexPanel = silhouette.copy(merged = characters.merged)

    /**
     * What's in a slot. It follows the same structure as [PanelSlot]:
     * hexagon or merged hexagons → character layout, corner key → function layout.
     * Null if the slot is free.
     */
    fun keyAt(slot: PanelSlot): KeyAssignment? = when (slot) {
        is PanelSlot.Cell -> characters.cells[slot.coord]
        // Every cell of a merged key holds the same thing (CharacterLayout checks it).
        is PanelSlot.Merged -> characters.cells[slot.cells.first()]
        is PanelSlot.Corner -> functions.corners[slot.position]?.let { KeyAssignment.Function(it) }
    }

    fun withCharacters(layout: CharacterLayout): Keyboard = copy(characters = layout)

    fun withFunctions(layout: FunctionLayout): Keyboard = copy(functions = layout)
}
