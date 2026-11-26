package pol.rubiano.eyesensekeyboard.features.keyboard.data.local

import kotlinx.coroutines.flow.Flow
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId

/**
 * Remembers which layouts are in use: one for the characters and one for the corner functions.
 * It only stores the two ids. Whether they still point to a layout, and what to use when they
 * don't, is decided by
 * [LayoutDataRepository][pol.rubiano.eyesensekeyboard.features.keyboard.data.datarepositories.LayoutDataRepository].
 *
 * Two implementations: DataStore (the real one) and memory (for tests).
 */
interface ActiveLayoutLocalDataSource {
    /** Null until a character layout has been activated. */
    val characterLayoutId: Flow<LayoutId?>

    /** Null until a function layout has been activated. */
    val functionLayoutId: Flow<LayoutId?>

    suspend fun setCharacterLayoutId(id: LayoutId)
    suspend fun setFunctionLayoutId(id: LayoutId)
}
