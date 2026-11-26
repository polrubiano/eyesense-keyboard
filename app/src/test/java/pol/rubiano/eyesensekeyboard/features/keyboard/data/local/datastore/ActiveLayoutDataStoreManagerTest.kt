package pol.rubiano.eyesensekeyboard.features.keyboard.data.local.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.LayoutId
import java.io.File

/** The real DataStore, writing to a temporary file: plain JVM, no phone or Robolectric needed. */
class ActiveLayoutDataStoreManagerTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Lazy: the temporary folder only exists once the test is running, not when the class is built.
    private val manager by lazy {
        ActiveLayoutDataStoreManager(
            PreferenceDataStoreFactory.create(scope = scope, produceFile = { File(folder.root, "settings.preferences_pb") }),
        )
    }

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun `nothing is active at the start`() = runBlocking {
        assertNull(manager.characterLayoutId.first())
        assertNull(manager.functionLayoutId.first())
    }

    @Test
    fun `each id is stored and read back on its own`() = runBlocking {
        manager.setCharacterLayoutId(LayoutId("user.1"))
        manager.setFunctionLayoutId(LayoutId("user.f"))
        assertEquals(LayoutId("user.1"), manager.characterLayoutId.first())
        assertEquals(LayoutId("user.f"), manager.functionLayoutId.first())
    }

    @Test
    fun `setting it again replaces the previous id`() = runBlocking {
        manager.setCharacterLayoutId(LayoutId("user.1"))
        manager.setCharacterLayoutId(LayoutId("user.2"))
        assertEquals(LayoutId("user.2"), manager.characterLayoutId.first())
    }

    @Test
    fun `changing the character layout doesn't make the function id emit`() = runBlocking {
        manager.setFunctionLayoutId(LayoutId("user.f"))
        val emissions = async { manager.functionLayoutId.take(2).toList() }
        manager.setCharacterLayoutId(LayoutId("user.1"))
        manager.setFunctionLayoutId(LayoutId("user.g"))
        // Only two emissions: the first value and the real change. The character write didn't count.
        assertEquals(listOf(LayoutId("user.f"), LayoutId("user.g")), withTimeout(5_000) { emissions.await() })
    }
}
