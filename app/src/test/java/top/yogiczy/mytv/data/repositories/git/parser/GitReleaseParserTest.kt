package top.yogiczy.mytv.data.repositories.git.parser

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Gitee/GitHub release 解析单元测试。
 *
 * 覆盖正常解析、v 前缀、空 assets、缺失字段与 null 字段等边界,
 * 防止发布无附件/非语义化 tag 时检查更新崩溃。
 */
class GiteeGitReleaseParserTest {

    private val parser = GiteeGitReleaseParser()

    @Test
    fun `parse extracts version downloadUrl and description`() = runBlocking {
        val json = """
            {
                "tag_name": "v1.4.4",
                "body": "更新说明",
                "assets": [
                    { "browser_download_url": "https://gitee.com/x/releases/download/v1.4.4/app.apk" }
                ]
            }
        """.trimIndent()

        val release = parser.parse(json)

        assertEquals("1.4.4", release.version)
        assertEquals("https://gitee.com/x/releases/download/v1.4.4/app.apk", release.downloadUrl)
        assertEquals("更新说明", release.description)
    }

    @Test
    fun `parse strips v prefix only when present`() = runBlocking {
        assertEquals("1.4.4", parser.parse("""{"tag_name": "1.4.4"}""").version)
    }

    @Test
    fun `parse tolerates empty assets`() = runBlocking {
        val release = parser.parse("""{"tag_name": "v1.4.4", "assets": []}""")

        assertEquals("1.4.4", release.version)
        assertEquals("", release.downloadUrl)
    }

    @Test
    fun `parse tolerates missing fields`() = runBlocking {
        val release = parser.parse("{}")

        assertEquals("", release.version)
        assertEquals("", release.downloadUrl)
        assertEquals("", release.description)
    }

    @Test
    fun `parse tolerates null body and assets`() = runBlocking {
        val release = parser.parse("""{"tag_name": "v1.4.4", "body": null, "assets": null}""")

        assertEquals("1.4.4", release.version)
        assertEquals("", release.downloadUrl)
        assertEquals("", release.description)
    }
}

class GithubGitReleaseParserTest {

    private val parser = GithubGitReleaseParser()

    @Test
    fun `parse prepends proxy to download url`() = runBlocking {
        val json = """
            {
                "tag_name": "v1.4.4",
                "assets": [
                    { "browser_download_url": "https://github.com/x/releases/download/v1.4.4/app.apk" }
                ]
            }
        """.trimIndent()

        val release = parser.parse(json)

        assertEquals("1.4.4", release.version)
        assertEquals(
            "https://mirror.ghproxy.com/https://github.com/x/releases/download/v1.4.4/app.apk",
            release.downloadUrl,
        )
    }
}
