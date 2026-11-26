package pol.rubiano.eyesensekeyboard.features.keyboard.data.local.db.mappers

import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.CharacterKeyEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.CharacterLayoutEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.CharacterLayoutWithKeys
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.FunctionKeyEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.FunctionLayoutEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.FunctionLayoutWithKeys
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.KeyKind
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.CharacterLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.FunctionLayout
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAction
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.KeyAssignment
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutOrigin
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.CornerPosition
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexCoord

/*
 * The only place that knows both shapes: the domain (maps, sealed types) and the tables
 * (flat rows of Strings and Ints). Entities never leave the data layer, and the domain
 * never learns there's a database.
 *
 * Writing only accepts what the tables can hold. Reading fails loudly on anything it doesn't
 * recognise: better to notice corrupt data than to show a keyboard with missing keys.
 */

// ---- Character layouts ----

/**
 * One row for the layout and one per hexagon. Each merged key gets a group number (its
 * position in [merged]), and every hexagon of that key carries it.
 */
fun CharacterLayout.toEntity(): CharacterLayoutWithKeys {
    requireUser(origin, name)
    val groupOf: Map<HexCoord, Int> =
        merged.withIndex().flatMap { (index, group) -> group.map { it to index } }.toMap()
    val keys = cells.map { (coord, assignment) ->
        val (kind, value) = assignment.toStored()
        CharacterKeyEntity(id.value, coord.q, coord.r, kind, value, groupOf[coord])
    }
    return CharacterLayoutWithKeys(CharacterLayoutEntity(id.value, name), keys)
}

/** Back to the domain: hexagons with the same group number become one merged key again. */
fun CharacterLayoutWithKeys.toDomain(): CharacterLayout {
    val cells = keys.associate { HexCoord(it.q, it.r) to it.toAssignment() }
    val merged = keys
        .filter { it.mergedGroup != null }
        .groupBy { it.mergedGroup }
        .toSortedMap(compareBy { it })
        .values
        .map { group -> group.map { HexCoord(it.q, it.r) }.toSet() }
    return CharacterLayout(LayoutId(layout.id), layout.name, LayoutOrigin.USER, cells, merged)
}

/** Text → its value. Function → its stable id. */
private fun KeyAssignment.toStored(): Pair<KeyKind, String> = when (this) {
    is KeyAssignment.Text -> KeyKind.TEXT to value
    is KeyAssignment.Function -> KeyKind.FUNCTION to action.id
    // Choices only exist while a menu is open (Keyboard.showing), and menus aren't saved.
    is KeyAssignment.Choice -> throw IllegalArgumentException("A menu choice can't be stored: $this")
}

private fun CharacterKeyEntity.toAssignment(): KeyAssignment = when (kind) {
    KeyKind.TEXT -> KeyAssignment.Text(value)
    KeyKind.FUNCTION -> KeyAssignment.Function(actionOf(value))
}

// ---- Function layouts ----

fun FunctionLayout.toEntity(): FunctionLayoutWithKeys {
    requireUser(origin, name)
    val keys = corners.map { (position, action) -> FunctionKeyEntity(id.value, position.name, action.id) }
    return FunctionLayoutWithKeys(FunctionLayoutEntity(id.value, name), keys)
}

fun FunctionLayoutWithKeys.toDomain(): FunctionLayout {
    val corners = keys.associate { cornerOf(it.corner) to actionOf(it.actionId) }
    return FunctionLayout(LayoutId(layout.id), layout.name, LayoutOrigin.USER, corners)
}

// ---- Shared ----

/** The tables have no origin column: whatever is stored is a user layout. */
private fun requireUser(origin: LayoutOrigin, name: String) {
    require(origin == LayoutOrigin.USER) { "Only user layouts are stored; \"$name\" is a $origin" }
}

private fun actionOf(id: String): KeyAction =
    requireNotNull(KeyAction.fromId(id)) { "Unknown function id in the database: \"$id\"" }

private fun cornerOf(name: String): CornerPosition =
    requireNotNull(CornerPosition.entries.firstOrNull { it.name == name }) { "Unknown corner in the database: \"$name\"" }
