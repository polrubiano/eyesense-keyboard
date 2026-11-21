package pol.rubiano.eyesensekeyboard.keyboard.domain

import pol.rubiano.eyesensekeyboard.hexgrid.domain.HexCoord
import pol.rubiano.eyesensekeyboard.hexgrid.domain.HexPanel

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
 * in [ranked] is not.
 */
fun characterLayoutByFrequency(
    id: LayoutId,
    name: String,
    panel: HexPanel,
    ranked: List<String>,
    pinned: Map<HexCoord, KeyAssignment> = emptyMap(),
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
    return CharacterLayout(id, name, origin, pinned + filled)
}
