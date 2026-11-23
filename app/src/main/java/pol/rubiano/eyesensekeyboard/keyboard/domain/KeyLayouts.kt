package pol.rubiano.eyesensekeyboard.keyboard.domain

import pol.rubiano.eyesensekeyboard.hexgrid.domain.CornerPosition
import pol.rubiano.eyesensekeyboard.hexgrid.domain.HexCoord

/** Stable identifier of a layout (e.g. "preset.es"). */
@JvmInline
value class LayoutId(val value: String) {
    init {
        require(value.isNotBlank()) { "A layout id can't be blank" }
    }
}

/** Where a layout comes from. Presets are read-only: to change one, you copy it. */
enum class LayoutOrigin { PRESET, USER }

/**
 * Hexagon layout: characters by default, plus functions if the user wants them.
 * It's a **partial** map: a hexagon with no entry is a free slot.
 */
data class CharacterLayout(
    val id: LayoutId,
    val name: String,
    val origin: LayoutOrigin,
    val cells: Map<HexCoord, KeyAssignment>,
) {
    fun assign(coord: HexCoord, assignment: KeyAssignment): CharacterLayout =
        edited { copy(cells = cells + (coord to assignment)) }

    fun clear(coord: HexCoord): CharacterLayout = edited { copy(cells = cells - coord) }

    fun swap(a: HexCoord, b: HexCoord): CharacterLayout = edited { copy(cells = cells.swapped(a, b)) }

    fun copyAsUser(newId: LayoutId, newName: String): CharacterLayout =
        copy(id = newId, name = newName, origin = LayoutOrigin.USER)

    private inline fun edited(change: CharacterLayout.() -> CharacterLayout): CharacterLayout {
        checkEditable(origin, name)
        return change()
    }
}

/**
 * Corner key layout: **functions only**. The value type ([KeyAction], not [KeyAssignment])
 * makes it impossible to put a character in a corner key: the compiler doesn't allow it.
 */
data class FunctionLayout(
    val id: LayoutId,
    val name: String,
    val origin: LayoutOrigin,
    val corners: Map<CornerPosition, KeyAction>,
) {
    fun assign(position: CornerPosition, action: KeyAction): FunctionLayout =
        edited { copy(corners = corners + (position to action)) }

    fun clear(position: CornerPosition): FunctionLayout = edited { copy(corners = corners - position) }

    fun swap(a: CornerPosition, b: CornerPosition): FunctionLayout =
        edited { copy(corners = corners.swapped(a, b)) }

    fun copyAsUser(newId: LayoutId, newName: String): FunctionLayout =
        copy(id = newId, name = newName, origin = LayoutOrigin.USER)

    private inline fun edited(change: FunctionLayout.() -> FunctionLayout): FunctionLayout {
        checkEditable(origin, name)
        return change()
    }
}

private fun checkEditable(origin: LayoutOrigin, name: String) {
    check(origin == LayoutOrigin.USER) {
        "The preset \"$name\" can't be edited: copy it first with copyAsUser()"
    }
}

/** Swaps the values of two keys. If one of them is empty, the other one moves into its place. */
private fun <K, V> Map<K, V>.swapped(a: K, b: K): Map<K, V> {
    val valueA = this[a]
    val valueB = this[b]
    return toMutableMap().apply {
        if (valueB != null) put(a, valueB) else remove(a)
        if (valueA != null) put(b, valueA) else remove(b)
    }
}
