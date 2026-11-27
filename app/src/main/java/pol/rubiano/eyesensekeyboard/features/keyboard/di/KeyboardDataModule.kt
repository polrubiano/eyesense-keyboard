package pol.rubiano.eyesensekeyboard.features.keyboard.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pol.rubiano.eyesensekeyboard.features.keyboard.data.datarepositories.LayoutDataRepository
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.daos.LayoutDao
import pol.rubiano.eyesensekeyboard.features.keyboard.data.local.db.KeyboardDatabase
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.repositories.LayoutRepository
import javax.inject.Singleton

/**
 * Tells Hilt how to build the keyboard's data layer: the layouts. Everything is a singleton: one
 * database and one repository for the whole app. (Which layout the user chose is a setting: it's
 * built by `KeyboardSettingsModule`.)
 *
 * ```
 * LayoutRepository ──@Binds──► LayoutDataRepository
 *                                   └── LayoutDbLocalDataSource
 *                                          └── LayoutDao ◄── KeyboardDatabase
 * ```
 *
 * - `@Binds`: "when someone asks for this interface, give them this class". The class is
 *   built through its `@Inject constructor`. The data source needs no binding: it's a
 *   concrete class (one implementation), and Hilt builds it on its own.
 * - `@Provides` (in the companion): for what can't have an `@Inject constructor`, such as
 *   the Room database, which is built with `Room.databaseBuilder`.
 *
 * The database lives here and not in `app/di/` because only the keyboard uses it. If another
 * feature ever needs it, it moves to `app/`.
 */
@Module
@InstallIn(SingletonComponent::class)
interface KeyboardDataModule {

    @Binds
    @Singleton
    fun bindLayoutRepository(repository: LayoutDataRepository): LayoutRepository

    companion object {

        @Provides
        @Singleton
        fun provideKeyboardDatabase(@ApplicationContext context: Context): KeyboardDatabase =
            Room.databaseBuilder(context, KeyboardDatabase::class.java, "keyboard.db").build()

        @Provides
        fun provideLayoutDao(database: KeyboardDatabase): LayoutDao = database.layoutDao()
    }
}
