package top.yogiczy.mytv.data.repositories

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.yogiczy.mytv.AppGlobal
import java.io.File

/**
 * [FileCacheRepository] 单元测试。
 *
 * 覆盖缓存命中、过期刷新、清除缓存与原子写入(tmp+rename)行为。
 */
class FileCacheRepositoryTest {

    private lateinit var cacheDir: File

    @Before
    fun setUp() {
        cacheDir = File(System.getProperty("java.io.tmpdir"), "mytv-test-${System.nanoTime()}")
        cacheDir.mkdirs()
        AppGlobal.cacheDir = cacheDir
    }

    @After
    fun tearDown() {
        cacheDir.deleteRecursively()
    }

    private class TestCacheRepository(fileName: String) : FileCacheRepository(fileName) {
        var refreshCount = 0
            private set

        suspend fun get(cacheTime: Long, value: String): String = getOrRefresh(cacheTime) {
            refreshCount++
            value
        }
    }

    @Test
    fun `getOrRefresh caches data and does not refresh before expiry`() = runBlocking {
        val repo = TestCacheRepository("cache-test.txt")

        val first = repo.get(60_000, "data-v1")
        val second = repo.get(60_000, "data-v2")

        assertEquals("data-v1", first)
        // 缓存未过期时不会再次调用刷新
        assertEquals("data-v1", second)
        assertEquals(1, repo.refreshCount)
    }

    @Test
    fun `getOrRefresh refreshes when cache expired`() = runBlocking {
        val repo = TestCacheRepository("cache-test-expiry.txt")

        repo.get(60_000, "data-v1")
        // cacheTime=0 时恒视为过期,应重新拉取
        val second = repo.get(0, "data-v2")

        assertEquals("data-v2", second)
        assertEquals(2, repo.refreshCount)
    }

    @Test
    fun `clearCache removes cache file`() = runBlocking {
        val repo = TestCacheRepository("cache-test-clear.txt")

        repo.get(60_000, "data-v1")
        assertTrue(File(cacheDir, "cache-test-clear.txt").exists())

        repo.clearCache()
        assertFalse(File(cacheDir, "cache-test-clear.txt").exists())
    }

    @Test
    fun `setCacheData leaves no tmp file after write`() = runBlocking {
        val repo = TestCacheRepository("cache-test-tmp.txt")

        repo.get(60_000, "data-v1")

        assertTrue(File(cacheDir, "cache-test-tmp.txt").exists())
        assertFalse(File(cacheDir, "cache-test-tmp.txt.tmp").exists())
    }
}
