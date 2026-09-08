package com.snatik.matches.data

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameTheme
import com.snatik.matches.game.progression.Progress
import com.snatik.matches.game.progression.RoundResult
import com.snatik.matches.game.progression.RoundSpec
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressStoreTest {

    private val T = GameTheme.ANIMALS

    private val dir = File(System.getProperty("java.io.tmpdir"), "progress-store-test-${System.nanoTime()}").apply { mkdirs() }
    private val file = File(dir, ProgressStore.FILE_NAME)
    private val dispatcher = StandardTestDispatcher()
    private val noLegacy: (GameTheme, Difficulty) -> RoundResult? = { _, _ -> null }

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain(); dir.deleteRecursively() }

    private fun store(legacy: (GameTheme, Difficulty) -> RoundResult? = noLegacy) = ProgressStore(file, legacy, TestScope(dispatcher), dispatcher)

    @Test
    fun `starts empty, records, and reloads what it wrote`() = runTest(dispatcher) {
        val store = store()
        assertEquals(Progress.EMPTY, store.progress.value)
        assertFalse("nothing to persist yet", file.exists())
        store.record(RoundSpec(T, Difficulty.LEVEL_1, 1), RoundResult(3, 21))
        assertEquals(RoundResult(3, 21), store.progress.value.resultOf(RoundSpec(T, Difficulty.LEVEL_1, 1)))
        advanceUntilIdle()
        assertTrue(file.exists())
        val reloaded = store { _, _ -> error("legacy must not be consulted when a file exists") }
        assertEquals(store.progress.value, reloaded.progress.value)
    }

    @Test
    fun `migrates 1x results into round 1 on first start and writes them`() = runTest(dispatcher) {
        val store = store { t, d -> if (t == GameTheme.MONSTERS && d == Difficulty.LEVEL_2) RoundResult(2, 48) else null }
        assertEquals(RoundResult(2, 48), store.progress.value.resultOf(RoundSpec(GameTheme.MONSTERS, Difficulty.LEVEL_2, 1)))
        assertEquals(1, store.progress.value.entries.size)
        advanceUntilIdle()
        assertTrue("migrated progress is persisted", file.exists())
        assertEquals(store.progress.value, store().progress.value)
    }

    @Test
    fun `reset forgets everything and writes an empty file`() = runTest(dispatcher) {
        val store = store()
        store.record(RoundSpec(T, Difficulty.LEVEL_1, 1), RoundResult(3, 21))
        advanceUntilIdle()
        store.reset()
        advanceUntilIdle()
        assertEquals(Progress.EMPTY, store.progress.value)
        assertEquals(Progress.EMPTY, store { _, _ -> error("no migration after a reset") }.progress.value)
    }

    @Test
    fun `an unreadable file means empty progress, not a crash`() = runTest(dispatcher) {
        file.writeText("   not a progress file")
        assertEquals(Progress.EMPTY, store().progress.value)
    }

    @Test
    fun `a worse result changes nothing and writes nothing`() = runTest(dispatcher) {
        val store = store()
        store.record(RoundSpec(T, Difficulty.LEVEL_1, 1), RoundResult(3, 21))
        advanceUntilIdle()
        val written = file.readText()
        file.writeText("sentinel")
        store.record(RoundSpec(T, Difficulty.LEVEL_1, 1), RoundResult(1, 5))
        advanceUntilIdle()
        assertEquals("no write happened", "sentinel", file.readText())
        assertEquals(store.progress.value, ProgressCodec.decode(written))
    }
}
