package pol.rubiano.eyesensekeyboard.keyboard.domain

import pol.rubiano.eyesensekeyboard.hexgrid.domain.CornerPosition
import pol.rubiano.eyesensekeyboard.hexgrid.domain.HexCoord
import pol.rubiano.eyesensekeyboard.hexgrid.domain.HexPanel

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
 *
 * [merged] says, explicitly, which hexagons act as **one key** (e.g. the space bar).
 * Two neighbours with the same content are NOT merged unless they're listed here.
 * A merged key is a single key, so:
 * - all its hexagons hold the same thing (checked on creation);
 * - editing any of its hexagons edits the whole key.
 */
data class CharacterLayout(
    val id: LayoutId,
    val name: String,
    val origin: LayoutOrigin,
    val cells: Map<HexCoord, KeyAssignment>,
    val merged: List<Set<HexCoord>> = emptyList(),
) {
    init {
        merged.forEach { group ->
            val contents = group.map { cells[it] }.toSet()
            require(contents.size == 1) { "The hexagons of a merged key must hold the same thing: $group → $contents" }
        }
    }

    /** The hexagons of the key [coord] belongs to: its merged group, or just itself. */
    fun keyCells(coord: HexCoord): Set<HexCoord> = merged.firstOrNull { coord in it } ?: setOf(coord)

    fun assign(coord: HexCoord, assignment: KeyAssignment): CharacterLayout =
        edited { withKey(coord, assignment) }

    fun clear(coord: HexCoord): CharacterLayout = edited { withKey(coord, null) }

    /** Exchanges the contents of two keys. If one of them is empty, the other one moves. */
    fun swap(a: HexCoord, b: HexCoord): CharacterLayout = edited {
        if (b in keyCells(a)) this else withKey(a, cells[b]).withKey(b, cells[a])
    }

    /** Puts [value] (or nothing) in every hexagon of the key [coord] belongs to. */
    private fun withKey(coord: HexCoord, value: KeyAssignment?): CharacterLayout {
        val keyCells = keyCells(coord)
        return copy(cells = if (value == null) cells - keyCells else cells + keyCells.associateWith { value })
    }

    fun copyAsUser(newId: LayoutId, newName: String): CharacterLayout =
        copy(id = newId, name = newName, origin = LayoutOrigin.USER)

    private inline fun edited(change: CharacterLayout.() -> CharacterLayout): CharacterLayout {
        checkEditable(origin, name)
        return change()
    }
}

/**
 * Builds a layout in two steps:
 * 1. Places the **pinned** keys ([pinned]) at their exact coordinates
 *    (for example, the space bar).
 * 2. Fills the remaining hexagons with [ranked], a list sorted by frequency (most
 *    used first), following the panel's spiral order and skipping the pinned cells.
 *
 * The panel's hexagons ([HexPanel.cells]) already come out sorted by closeness to the
 * centre, so the most frequent key goes in the centre, the next ones in the first ring, and
 * so on. Because the pinned cells get skipped, the letters reorder themselves around
 * them. The algorithm is the same for every language: adding a language = adding a list.
 *
 * Repeating a pinned key is allowed (several space hexagons); repeating a letter
 * in [ranked] is not. [merged] says which pinned hexagons act as one key.
 */
fun characterLayoutByFrequency(
    id: LayoutId,
    name: String,
    panel: HexPanel,
    ranked: List<String>,
    pinned: Map<HexCoord, KeyAssignment> = emptyMap(),
    merged: List<Set<HexCoord>> = emptyList(),
    origin: LayoutOrigin = LayoutOrigin.PRESET,
): CharacterLayout {
    val outside = pinned.keys.filterNot { it in panel }
    require(outside.isEmpty()) { "There are pinned keys outside the panel: $outside" }
    require(ranked.size + pinned.size <= panel.cells.size) {
        "${ranked.size} keys + ${pinned.size} pinned don't fit in the panel's ${panel.cells.size} hexagons"
    }
    require(ranked.toSet().size == ranked.size) { "There are repeated keys in the list: $ranked" }

    val freeCells = panel.cells.filterNot { it in pinned }
    val filled = freeCells.zip(ranked) { coord, text -> coord to KeyAssignment.Text(text) }
    return CharacterLayout(id, name, origin, pinned + filled, merged)
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
