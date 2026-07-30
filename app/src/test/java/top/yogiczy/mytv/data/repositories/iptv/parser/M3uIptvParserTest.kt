package top.yogiczy.mytv.data.repositories.iptv.parser

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import top.yogiczy.mytv.data.entities.IptvGroupList

/**
 * [M3uIptvParser] 单元测试。
 *
 * 这是历史 bug 高发区(参见 CHANGELOG 1.4.0「修复未知直播源格式导致闪退」),
 * 覆盖标准格式、tvg-name 回退、group-title 默认值、空行、多 URL 合并等场景。
 */
class M3uIptvParserTest {

    private val parser = M3uIptvParser()

    @Test
    fun `isSupport returns true for EXTM3U header`() {
        assertTrue(parser.isSupport("http://x/live.m3u", "#EXTM3U\n#EXTINF:-1,..."))
    }

    @Test
    fun `isSupport returns false when header missing`() {
        assertFalse(parser.isSupport("http://x", "CCTV-1,http://example/#genre#"))
        assertFalse(parser.isSupport("http://x", ""))
    }

    @Test
    fun `parse parses standard channel with tvg-name and group-title`() = runBlocking {
        val data = """
            #EXTM3U
            #EXTINF:-1 tvg-name="cctv1" group-title="央视",CCTV-1
            http://example.com/cctv1.m3u8
        """.trimIndent()

        val result: IptvGroupList = parser.parse(data)

        assertEquals(1, result.size)
        assertEquals("央视", result[0].name)
        assertEquals(1, result[0].iptvList.size)

        val channel = result[0].iptvList[0]
        assertEquals("CCTV-1", channel.name)
        assertEquals("cctv1", channel.channelName)
        assertEquals(listOf("http://example.com/cctv1.m3u8"), channel.urlList)
    }

    @Test
    fun `parse falls back to trailing name when tvg-name is absent`() = runBlocking {
        val data = """
            #EXTM3U
            #EXTINF:-1 group-title="央视",CCTV-综合
            http://example.com/cctv.m3u8
        """.trimIndent()

        val channel = parser.parse(data)[0].iptvList[0]

        // tvg-name 缺失时,channelName 应回退为逗号后的频道名
        assertEquals("CCTV-综合", channel.name)
        assertEquals("CCTV-综合", channel.channelName)
    }

    @Test
    fun `parse defaults group-title to 其他 when missing`() = runBlocking {
        val data = """
            #EXTM3U
            #EXTINF:-1 tvg-name="cctv1",CCTV-1
            http://example.com/cctv1.m3u8
        """.trimIndent()

        val group = parser.parse(data)[0]

        assertEquals("其他", group.name)
    }

    @Test
    fun `parse groups channels by group-title`() = runBlocking {
        val data = """
            #EXTM3U
            #EXTINF:-1 tvg-name="cctv1" group-title="央视",CCTV-1
            http://a/cctv1.m3u8
            #EXTINF:-1 tvg-name="hunan" group-title="地方",湖南卫视
            http://a/hunan.m3u8
            #EXTINF:-1 tvg-name="cctv2" group-title="央视",CCTV-2
            http://a/cctv2.m3u8
        """.trimIndent()

        val result = parser.parse(data)

        assertEquals(2, result.size)
        val groups = result.associateBy { it.name }
        assertEquals(2, groups["央视"]?.iptvList?.size)
        assertEquals(1, groups["地方"]?.iptvList?.size)
    }

    @Test
    fun `parse merges same-name channels into multiple urlList entries`() = runBlocking {
        // 同名不同地址应合并为一个频道,地址聚合成 urlList
        val data = """
            #EXTM3U
            #EXTINF:-1 tvg-name="cctv1" group-title="央视",CCTV-1
            http://line1/cctv1.m3u8
            #EXTINF:-1 tvg-name="cctv1" group-title="央视",CCTV-1
            http://line2/cctv1.m3u8
        """.trimIndent()

        val channels = parser.parse(data)[0].iptvList

        assertEquals(1, channels.size)
        assertEquals(
            listOf("http://line1/cctv1.m3u8", "http://line2/cctv1.m3u8"),
            channels[0].urlList,
        )
    }

    @Test
    fun `parse handles crlf line endings`() = runBlocking {
        val data =
            "#EXTM3U\r\n#EXTINF:-1 tvg-name=\"cctv1\" group-title=\"央视\",CCTV-1\r\nhttp://a/cctv1.m3u8\r\n"

        val channel = parser.parse(data)[0].iptvList[0]

        assertEquals("CCTV-1", channel.name)
        assertEquals("http://a/cctv1.m3u8", channel.urlList[0])
    }

    @Test
    fun `parse returns empty result for header-only input`() = runBlocking {
        val result = parser.parse("#EXTM3U\n")

        assertEquals(0, result.size)
    }
}
