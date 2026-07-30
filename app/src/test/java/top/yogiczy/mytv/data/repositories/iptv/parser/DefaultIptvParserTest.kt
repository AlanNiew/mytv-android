package top.yogiczy.mytv.data.repositories.iptv.parser

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [DefaultIptvParser] 单元测试。
 *
 * 作为解析链末端兜底,isSupport 恒为 true,返回固定的「不支持」提示。
 * 覆盖兜底契约,防止后续重构破坏降级行为。
 */
class DefaultIptvParserTest {

    private val parser = DefaultIptvParser()

    @Test
    fun `isSupport always returns true for any input`() {
        assertTrue(parser.isSupport("http://x", "anything"))
        assertTrue(parser.isSupport("", ""))
        assertTrue(parser.isSupport("http://x", "#EXTM3U"))
    }

    @Test
    fun `parse returns fixed unsupported-format notice`() = runBlocking {
        val result = parser.parse("任意无法识别的数据")

        assertEquals(1, result.size)
        // 分组名是用户可见的提示文案,包含格式说明
        assertTrue(
            "分组名应包含格式不支持提示",
            result[0].name.contains("不支持当前直播源链接格式"),
        )
    }

    @Test
    fun `parse lists supported formats m3u and tvbox`() = runBlocking {
        val channels = parser.parse("")[0].iptvList

        val names = channels.map { it.name }
        assertEquals(listOf("m3u", "tvbox"), names)
    }
}
