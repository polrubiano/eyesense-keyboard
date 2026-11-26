package pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

/*
 * How a user layout is stored: one row for the layout, and one row per key.
 *
 *   character_layouts          character_keys
 *   id       name              layoutId  q   r  kind      value   mergedGroup
 *   user.1   Castellano 2      user.1    0   0  TEXT      e       null
 *                              user.1    0   2  TEXT      " "     0          ← the space bar:
 *                              user.1    1   2  TEXT      " "     0            same group = one key
 *                              user.1    1  -1  FUNCTION  speak   null
 *
 * Only user layouts are stored: presets live in code (Presets.kt). That's why there's no
 * `origin` column, since everything here is USER.
 *
 * Columns hold plain values (String, Int), never domain types, so renaming a Kotlin class
 * can't silently break the data that's already saved. Mapping lives in LayoutMappers.kt.
 */

@Entity(tableName = "character_layouts")
data class CharacterLayoutEntity(
    @PrimaryKey val id: String,
    val name: String,
)

/**
 * One hexagon of a character layout. Deleting the layout deletes its keys (CASCADE).
 *
 * [mergedGroup]: hexagons with the same number form one key (e.g. the space bar); null if the
 * hexagon is a key on its own.
 */
@Entity(
    tableName = "character_keys",
    primaryKeys = ["layoutId", "q", "r"],
    foreignKeys = [
        ForeignKey(
            entity = CharacterLayoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["layoutId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CharacterKeyEntity(
    val layoutId: String,
    val q: Int,
    val r: Int,
    val kind: KeyKind,
    /** The text it writes ([KeyKind.TEXT]) or the function's stable id ([KeyKind.FUNCTION]). */
    val value: String,
    val mergedGroup: Int?,
)

/** What a stored key holds. Room saves it by name ("TEXT", "FUNCTION"). */
enum class KeyKind { TEXT, FUNCTION }

@Entity(tableName = "function_layouts")
data class FunctionLayoutEntity(
    @PrimaryKey val id: String,
    val name: String,
)

/** One corner of a function layout: which corner ([corner], by name) holds which function. */
@Entity(
    tableName = "function_keys",
    primaryKeys = ["layoutId", "corner"],
    foreignKeys = [
        ForeignKey(
            entity = FunctionLayoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["layoutId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class FunctionKeyEntity(
    val layoutId: String,
    val corner: String,
    val actionId: String,
)

/** A character layout with all its keys, read in one go thanks to [Relation]. */
data class CharacterLayoutWithKeys(
    @Embedded val layout: CharacterLayoutEntity,
    @Relation(parentColumn = "id", entityColumn = "layoutId")
    val keys: List<CharacterKeyEntity>,
)

/** A function layout with all its corners. */
data class FunctionLayoutWithKeys(
    @Embedded val layout: FunctionLayoutEntity,
    @Relation(parentColumn = "id", entityColumn = "layoutId")
    val keys: List<FunctionKeyEntity>,
)
