package pol.rubiano.eyesensekeyboard.features.keyboardsettings.presentation.components

import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.Point
import pol.rubiano.eyesensekeyboard.features.keyboard.presentation.hexgrid.PolygonShape
import kotlin.math.sqrt

/**
 * A **flat-top** hexagon filling its box: lying down, unlike the keyboard's pointy-top keys.
 * Lying down, it's only √3·r tall instead of 2r, so a column of them never needs the
 * keyboard's whole height. Stacked, they leave a V-shaped notch between each pair on the right,
 * which is where each setting row's point fits.
 * ```
 *     (.25, 0)   (.75, 0)
 *   (0, .5)          (1, .5)
 *     (.25, 1)   (.75, 1)
 * ```
 * The box must be [FLAT_HEXAGON_ASPECT] wide for each unit of height (2 : √3).
 */
internal val FlatHexagonShape = PolygonShape(
    listOf(
        Point(0.25f, 0f), Point(0.75f, 0f), Point(1f, 0.5f),
        Point(0.75f, 1f), Point(0.25f, 1f), Point(0f, 0.5f),
    ),
)

/** Width / height of a flat-top hexagon's box. */
internal val FLAT_HEXAGON_ASPECT: Float = 2f / sqrt(3f)
