package pol.rubiano.eyesensekeyboard.keyboard.domain

import pol.rubiano.eyesensekeyboard.hexgrid.domain.HexPanel
import pol.rubiano.eyesensekeyboard.hexgrid.domain.PanelSlot

/**
 * The keyboard in use: a panel + a character layout + a function layout.
 *
 * The two layouts are independent: changing language ([withCharacters]) keeps the
 * functions where they were.
 */
data class Keyboard(
    val panel: HexPanel,
    val characters: CharacterLayout,
    val functions: FunctionLayout,
) {
    init {
        val outside = characters.cells.keys.filterNot { it in panel }
        require(outside.isEmpty()) {
            "The layout \"${characters.name}\" has keys outside the panel: $outside"
        }
    }

    /**
     * What's in a slot. It follows the same structure as [PanelSlot]:
     * hexagon → character layout, corner key → function layout.
     * Null if the slot is free.
     */
    fun keyAt(slot: PanelSlot): KeyAssignment? = when (slot) {
        is PanelSlot.Cell -> characters.cells[slot.coord]
        is PanelSlot.Corner -> functions.corners[slot.position]?.let { KeyAssignment.Function(it) }
    }

    fun withCharacters(layout: CharacterLayout): Keyboard = copy(characters = layout)

    fun withFunctions(layout: FunctionLayout): Keyboard = copy(functions = layout)
}
