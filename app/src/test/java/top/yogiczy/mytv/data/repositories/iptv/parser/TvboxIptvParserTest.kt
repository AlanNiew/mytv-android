package top.yogiczy.mytv.data.repositories.iptv.parser

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import top.yogiczy.mytv.data.entities.IptvGroupList

/**
 * [TvboxIptvParser] 单元测试。
 *
 * 处理 #genre# 文本分组格式,频道行用逗号拆分、井号拆多个 URL。
 * 覆盖分组切换、多 URL、全角逗号、默认分组等场景。
 */
class TvboxIptvParserTest {

    private val parser = TvboxIptvParser()

    @Test
    fun `isSupport returns true when data contains genre marker`() {
        assertTrue(parser.isSupport("http://x", "央视,#genre#\nCCTV-1,http://a\n"))
    }

    @Test
    fun `isSupport returns false when no genre marker`() {
        assertFalse(parser.isSupport("http://x", "#EXTM3U\n#EXTINF:-1,..."))
        assertFalse(parser.isSupport("http://x", ""))
    }

    @Test
    fun `parse parses channels grouped by genre marker`() = runBlocking {
        val data = """
            央视,#genre#
            CCTV-1,http://a/cctv1.m3u8
            CCTV-2,http://a/cctv2.m3u8
        """.trimIndent()

        val result: IptvGroupList = parser.parse(data)

        assertEquals(1, result.size)
        assertEquals("央视", result[0].name)
        assertEquals(2, result[0].iptvList.size)

        val first = result[0].iptvList[0]
        assertEquals("CCTV-1", first.name)
        assertEquals("CCTV-1", first.channelName)
        assertEquals(listOf("http://a/cctv1.m3u8"), first.urlList)
    }

    @Test
    fun `parse splits multiple urls by hash`() = runBlocking {
        val data = """
            央视,#genre#
            CCTV-1,http://line1#http://line2#http://line3
        """.trimIndent()

        val channel = parser.parse(data)[0].iptvList[0]

        assertEquals(
            listOf("http://line1", "http://line2", "http://line3"),
            channel.urlList,
        )
    }

    @Test
    fun `parse handles multiple groups`() = runBlocking {
        val data = """
            央视,#genre#
            CCTV-1,http://a/cctv1
            地方,#genre#
            湖南卫视,http://a/hunan
        """.trimIndent()

        val result = parser.parse(data)

        assertEquals(2, result.size)
        assertEquals("央视", result[0].name)
        assertEquals("地方", result[1].name)
        assertEquals(1, result[0].iptvList.size)
        assertEquals(1, result[1].iptvList.size)
    }

    @Test
    fun `parse assigns channels before any genre to 其他 group`() = runBlocking {
        // groupName 初始为 null,遇到频道行前无 #genre# 时应归入默认分组
        val data = """
            未分组频道,http://a/b
            央视,#genre#
            CCTV-1,http://a/cctv1
        """.trimIndent()

        val result = parser.parse(data)

        assertEquals(2, result.size)
        val groups = result.associateBy { it.name }
        assertEquals(1, groups["其他"]?.iptvList?.size)
        assertEquals(1, groups["央视"]?.iptvList?.size)
    }

    @Test
    fun `parse normalizes fullwidth comma to halfwidth`() = runBlocking {
        // 代码中 replace("，", ",") 处理全角逗号,确保中文输入法下的逗号也能解析
        val data = "央视,#genre#\nCCTV-1，http://a/cctv1"

        val channel = parser.parse(data)[0].iptvList[0]

        assertEquals("CCTV-1", channel.name)
        assertEquals(listOf("http://a/cctv1"), channel.urlList)
    }

    @Test
    fun `parse skips comment and blank lines`() = runBlocking {
        val data = """
            央视,#genre#
            # 这是一条注释

            CCTV-1,http://a/cctv1
        """.trimIndent()

        val result = parser.parse(data)

        assertEquals(1, result[0].iptvList.size)
    }

    @Test
    fun `parse skips channel line without url`() = runBlocking {
        // res.size < 2 时跳过,防止只有频道名没有地址的行导致崩溃
        val data = """
            央视,#genre#
            只有名字没有逗号
            CCTV-1,http://a/cctv1
        """.trimIndent()

        val result = parser.parse(data)

        assertEquals(1, result[0].iptvList.size)
        assertEquals("CCTV-1", result[0].iptvList[0].name)
    }
}
