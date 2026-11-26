package pol.rubiano.eyesensekeyboard.features.keyboard.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.CharacterKeyEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.CharacterLayoutEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.CharacterLayoutWithKeys
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.FunctionKeyEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.FunctionLayoutEntity
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.entities.FunctionLayoutWithKeys

/**
 * Reads and writes user layouts. Room writes the implementation at build time (KSP).
 *
 * - Reads are a [Flow]: it emits again whenever the tables change, so whoever observes it
 *   always sees what's saved, with no manual refresh.
 * - Saving **replaces the whole layout**: delete it (its keys go with it, CASCADE) and insert
 *   it again. Inside a [Transaction], so nobody ever sees it half written, and the Flow emits
 *   once, at the end.
 */
@Dao
interface LayoutDao {

    @Transaction
    @Query("SELECT * FROM character_layouts ORDER BY name")
    fun characterLayouts(): Flow<List<CharacterLayoutWithKeys>>

    @Transaction
    @Query("SELECT * FROM function_layouts ORDER BY name")
    fun functionLayouts(): Flow<List<FunctionLayoutWithKeys>>

    @Transaction
    suspend fun save(layout: CharacterLayoutWithKeys) {
        deleteCharacterLayout(layout.layout.id)
        insert(layout.layout)
        insertCharacterKeys(layout.keys)
    }

    @Transaction
    suspend fun save(layout: FunctionLayoutWithKeys) {
        deleteFunctionLayout(layout.layout.id)
        insert(layout.layout)
        insertFunctionKeys(layout.keys)
    }

    @Query("DELETE FROM character_layouts WHERE id = :id")
    suspend fun deleteCharacterLayout(id: String)

    @Query("DELETE FROM function_layouts WHERE id = :id")
    suspend fun deleteFunctionLayout(id: String)

    @Insert
    suspend fun insert(layout: CharacterLayoutEntity)

    @Insert
    suspend fun insertCharacterKeys(keys: List<CharacterKeyEntity>)

    @Insert
    suspend fun insert(layout: FunctionLayoutEntity)

    @Insert
    suspend fun insertFunctionKeys(keys: List<FunctionKeyEntity>)
}
