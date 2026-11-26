package pol.rubiano.eyesensekeyboard.features.keyboard.domain

import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.CornerPosition
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexCoord
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexPanel

/** The layouts that ship with the app. */
object Presets {

    /** The EyeSense keyboard silhouette: rows of 5-6-7-6-5 (29 hexagons) + 4 corner keys. */
    val PANEL = HexPanel(rowsAroundCenter = 2, rings = 3)

    /**
     * Space bar: several hexagons merged into **one key** that writes a space. The hexagons
     * left over after placing the letters become part of the bar, so the panel ends up full.
     *
     * It sits at the **right-hand end of the bottom row** so that, visually, no letters
     * come "after" the space. Its position (ring 2-3) is to be checked with letter-metter.
     */
    private val SPANISH_SPACE_BAR = setOf(HexCoord(0, 2), HexCoord(1, 2))
    private val ENGLISH_SPACE_BAR = setOf(HexCoord(-1, 2), HexCoord(0, 2), HexCoord(1, 2))

    private fun spaceBar(coords: Set<HexCoord>): Map<HexCoord, KeyAssignment> =
        coords.associateWith { KeyAssignment.Text(" ") }

    private fun letters(sortedByFrequency: String): List<String> = sortedByFrequency.split(" ")

    /** 27 letters + 2 spaces = 29: full panel. */
    val SPANISH: CharacterLayout = characterLayoutByFrequency(
        id = LayoutId("preset.es"),
        name = "Castellano",
        panel = PANEL,
        ranked = letters("e a o s r n i d l c t u m p b g v y q h f z j ñ x k w"),
        pinned = spaceBar(SPANISH_SPACE_BAR),
        merged = listOf(SPANISH_SPACE_BAR),
    )

    /** 26 letters + 3 spaces = 29: full panel. */
    val ENGLISH: CharacterLayout = characterLayoutByFrequency(
        id = LayoutId("preset.en"),
        name = "English",
        panel = PANEL,
        ranked = letters("e t a o i n s h r d l c u m w f g y p b v k j x q z"),
        pinned = spaceBar(ENGLISH_SPACE_BAR),
        merged = listOf(ENGLISH_SPACE_BAR),
    )

    val DEFAULT_FUNCTIONS = FunctionLayout(
        id = LayoutId("preset.functions"),
        name = "Funciones",
        origin = LayoutOrigin.PRESET,
        corners = mapOf(
            CornerPosition.TOP_LEFT to KeyAction.ClearText,
            CornerPosition.TOP_RIGHT to KeyAction.DeleteCharacter,
            CornerPosition.BOTTOM_LEFT to KeyAction.SelectEmotion,
            CornerPosition.BOTTOM_RIGHT to KeyAction.Speak,
        ),
    )

    val characterLayouts: List<CharacterLayout> = listOf(SPANISH, ENGLISH)
    val functionLayouts: List<FunctionLayout> = listOf(DEFAULT_FUNCTIONS)
}
