package top.yogiczy.mytv.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [compareVersion] 单元测试。
 *
 * 覆盖数值段比较、缺段补零、非数字段容错与 v 前缀场景,
 * 防止发布非语义化 tag 时版本比对崩溃。
 */
class CompareVersionTest {

    @Test
    fun `compareVersion compares numeric parts`() {
        assertTrue("1.4.4".compareVersion("1.4.3") > 0)
        assertTrue("1.4.3".compareVersion("1.4.4") < 0)
        assertEquals(0, "1.4.4".compareVersion("1.4.4"))
    }

    @Test
    fun `compareVersion treats missing parts as zero`() {
        assertEquals(0, "1.4".compareVersion("1.4.0"))
        assertTrue("1.4.1".compareVersion("1.4") > 0)
    }

    @Test
    fun `compareVersion handles non-numeric segments without crash`() {
        // 非数字段按 0 处理,不抛 NumberFormatException
        assertEquals(0, "1.2.beta3".compareVersion("1.2.0"))
        // 预发布标签(以 - 分隔)小于正式版
        assertTrue("1.2.0-beta1".compareVersion("1.2.0") < 0)
        assertTrue("1.2.0".compareVersion("1.2.0-beta1") > 0)
    }

    @Test
    fun `compareVersion handles v prefix`() {
        // compareVersion 本身不剥离 v 前缀,带 v 的段按 0 处理;
        // 版本号剥离前缀由 git parser 的 removePrefix 完成
        assertTrue("1.4.4".compareVersion("v1.4.3") > 0)
        assertTrue("v1.4.4".compareVersion("1.4.4") < 0)
    }
}
