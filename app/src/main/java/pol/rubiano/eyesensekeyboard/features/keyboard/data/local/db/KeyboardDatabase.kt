package pol.rubiano.eyesensekeyboard.features.keyboard.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.daos.LayoutDao
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.CharacterKeyEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.CharacterLayoutEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.FunctionKeyEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.FunctionLayoutEntity

/**
 * The keyboard's database. Today it only holds user layouts; phrases and message history
 * will join it later as new tables.
 *
 * [version] goes up with every schema change, and each change needs a migration. The schema
 * of every version is exported to `app/schemas/` and committed: it's what migrations are
 * checked against.
 */
@Database(
    entities = [
        CharacterLayoutEntity::class,
        CharacterKeyEntity::class,
        FunctionLayoutEntity::class,
        FunctionKeyEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class KeyboardDatabase : RoomDatabase() {
    abstract fun layoutDao(): LayoutDao
}
