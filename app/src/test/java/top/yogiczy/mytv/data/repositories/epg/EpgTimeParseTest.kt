package top.yogiczy.mytv.data.repositories.epg

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

class EpgTimeParseTest {

    @Before
    fun setUp() {
        // 固定 JVM 时区,保证无时区偏移用例的确定性
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(null)
    }

    @Test
    fun 标准格式无时区偏移() {
        assertEquals(1631016000000L, parseEpgTime("20210907120000"))
    }

    @Test
    fun 标准格式带正时区偏移() {
        // 北京时间 12:00 = UTC 04:00
        assertEquals(1630987200000L, parseEpgTime("20210907120000 +0800"))
    }

    @Test
    fun 标准格式带负时区偏移() {
        // UTC-8 时区 12:00 = UTC 20:00
        assertEquals(1631044800000L, parseEpgTime("20210907120000 -0800"))
    }

    @Test
    fun 标准格式带零时区偏移() {
        assertEquals(1631016000000L, parseEpgTime("20210907120000 +0000"))
    }

    @Test
    fun 长度不足返回零() {
        assertEquals(0L, parseEpgTime("2021"))
        assertEquals(0L, parseEpgTime(""))
    }

    @Test
    fun 非法格式返回零() {
        assertEquals(0L, parseEpgTime("not-a-valid-time-string!"))
    }
}
