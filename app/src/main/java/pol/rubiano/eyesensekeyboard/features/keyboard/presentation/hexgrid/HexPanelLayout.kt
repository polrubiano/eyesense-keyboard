package pol.rubiano.eyesensekeyboard.features.keyboard.presentation.hexgrid

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexPanel
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.PanelGeometry
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.PanelSlot
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.Point
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.SlotOutline
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.outline
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.slotAt
import kotlin.math.roundToInt

/**
 * What each slot's content gets: its shape and a way to centre things optically.
 * It follows the same pattern as `BoxScope.align()` or `RowScope.weight()`.
 */
@Stable
interface HexSlotScope {
    /** The slot's outline (hexagon or corner key), ready for `background`, `border` or `clip`. */
    val shape: Shape

    /** Diameter of the label circle (one hexagon's inscribed circle): the space a label has without spilling out. */
    val labelAreaSize: Dp

    /** Places the element with its centre on the slot's label centre. */
    fun Modifier.alignToSlotCenter(): Modifier
}

/**
 * Generic hexagon panel. It knows nothing about keyboards: it lays out the slots of
 * [panel] in the space it is given and asks [slotContent] what to draw in each one.
 *
 * - The size is derived from the available space ([PanelGeometry.Companion.fit]); the
 *   composable takes up exactly the panel's size and its parent decides where to put it.
 * - [gap] is the visual separation between slots. It only affects the drawing.
 * - [cornerRounding] rounds the corners, as a fraction of the hexagon radius (0 = sharp),
 *   so the panel looks equally round on any screen. Also visual only: touch zones keep
 *   their sharp corners and still tile the panel with no dead zones.
 * - [onSlotClick] gets the slot that was tapped. Touch is detected **once, for the whole
 *   panel**, and resolved with geometry ([slotAt]): the zones follow the honeycomb exactly,
 *   with no overlaps or gaps, even though the slot boxes overlap.
 */
@Composable
fun HexPanelLayout(
    panel: HexPanel,
    modifier: Modifier = Modifier,
    gap: Dp = 4.dp,
    cornerRounding: Float = 0.15f,
    onSlotClick: (PanelSlot) -> Unit = {},
    slotContent: @Composable HexSlotScope.(PanelSlot) -> Unit,
) {
    // Always the latest version of the callback, without restarting pointerInput every time it changes.
    val currentOnSlotClick by rememberUpdatedState(onSlotClick)

    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val availableWidth = constraints.maxWidth.toFloat()
        val availableHeight =
            if (constraints.hasBoundedHeight) constraints.maxHeight.toFloat() else Float.POSITIVE_INFINITY

        val geometry = remember(panel, availableWidth, availableHeight) {
            PanelGeometry.fit(panel, availableWidth, availableHeight)
        }
        // Each slot shrinks by half the gap: two neighbours together add up to the whole gap.
        val insetPx = with(density) { gap.toPx() } / 2f
        val cornerRadiusPx = geometry.hexRadius * cornerRounding

        Box(
            Modifier
                .size(
                    width = with(density) { geometry.width.toDp() },
                    height = with(density) { geometry.height.toDp() },
                )
                // The tap's offset is already local to the panel: it's the same space as the geometry.
                .pointerInput(geometry) {
                    detectTapGestures { offset ->
                        geometry.slotAt(Point(offset.x, offset.y))?.let(currentOnSlotClick)
                    }
                }
        ) {
            panel.slots.forEach { slot ->
                key(slot) {
                    val outline = remember(geometry, slot, insetPx) { geometry.outline(slot).inset(insetPx) }
                    SlotBox(outline, cornerRadiusPx) { slotContent(slot) }
                }
            }
        }
    }
}

/** Places a box over the slot's outline (its bounding rectangle) and exposes the scope. */
@Composable
private fun SlotBox(
    outline: SlotOutline,
    cornerRadius: Float,
    content: @Composable HexSlotScope.() -> Unit,
) {
    val density = LocalDensity.current
    val left = outline.vertices.minOf { it.x }
    val top = outline.vertices.minOf { it.y }
    val boxWidth = outline.vertices.maxOf { it.x } - left
    val boxHeight = outline.vertices.maxOf { it.y } - top

    val scope = remember(outline, cornerRadius, density) {
        HexSlotScopeImpl(
            shape = PolygonShape(
                normalizedVertices = outline.vertices.map {
                    Point(
                        (it.x - left) / boxWidth,
                        (it.y - top) / boxHeight
                    )
                },
                cornerRadius = cornerRadius,
            ),
            labelAreaSize = with(density) { (outline.labelRadius * 2f).toDp() },
            labelCenter = Point(outline.labelCenter.x - left, outline.labelCenter.y - top),
        )
    }

    Box(
        Modifier
            .offset { IntOffset(left.roundToInt(), top.roundToInt()) }
            .size(with(density) { boxWidth.toDp() }, with(density) { boxHeight.toDp() })
    ) {
        scope.content()
    }
}

private class HexSlotScopeImpl(
    override val shape: Shape,
    override val labelAreaSize: Dp,
    private val labelCenter: Point,
) : HexSlotScope {

    override fun Modifier.alignToSlotCenter(): Modifier = layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        layout(placeable.width, placeable.height) {
            placeable.place(
                x = (labelCenter.x - placeable.width / 2f).roundToInt(),
                y = (labelCenter.y - placeable.height / 2f).roundToInt(),
            )
        }
    }
}
