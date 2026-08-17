package top.yogiczy.mytv.data.repositories.epg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import top.yogiczy.mytv.data.entities.EpgList
import java.io.InputStream
import java.io.Reader

/**
 * [EpgXmlParser] 单元测试。
 *
 * 通过注入 Fake XmlPullParser 在 JVM 上测试 channel/programme 解析、
 * 频道过滤、孤儿节目丢弃与时区偏移解析,不依赖 android.util.Xml。
 */
class EpgXmlParserTest {

    private fun parse(
        vararg events: Event,
        filteredChannels: List<String> = emptyList(),
    ): EpgList =
        EpgXmlParser { FakeXmlPullParser(events.toList()) }
            .parse("fixture", filteredChannels)

    @Test
    fun `parse builds epg from channels and programmes`() {
        val epgList = parse(
            Event.StartTag("channel", mapOf("id" to "cctv1")),
            Event.StartTag("display-name"),
            Event.Text("CCTV-1"),
            Event.EndTag("display-name"),
            Event.EndTag("channel"),
            Event.StartTag(
                "programme",
                mapOf(
                    "channel" to "cctv1",
                    "start" to "20210907120000 +0800",
                    "stop" to "20210907130000 +0800",
                ),
            ),
            Event.StartTag("title"),
            Event.Text("新闻联播"),
            Event.EndTag("title"),
            Event.EndTag("programme"),
        )

        assertEquals(1, epgList.size)
        val epg = epgList[0]
        assertEquals("CCTV-1", epg.channel)
        assertEquals(1, epg.programmes.size)

        val programme = epg.programmes[0]
        assertEquals("新闻联播", programme.title)
        assertEquals(3_600_000L, programme.endAt - programme.startAt)
    }

    @Test
    fun `parse filters channels by name`() {
        val epgList = parse(
            Event.StartTag("channel", mapOf("id" to "cctv1")),
            Event.StartTag("display-name"),
            Event.Text("CCTV-1"),
            Event.EndTag("display-name"),
            Event.EndTag("channel"),
            Event.StartTag("channel", mapOf("id" to "cctv2")),
            Event.StartTag("display-name"),
            Event.Text("CCTV-2"),
            Event.EndTag("display-name"),
            Event.EndTag("channel"),
            Event.StartTag(
                "programme",
                mapOf(
                    "channel" to "cctv2",
                    "start" to "20210907120000 +0800",
                    "stop" to "20210907130000 +0800",
                ),
            ),
            Event.StartTag("title"),
            Event.Text("被过滤的节目"),
            Event.EndTag("title"),
            Event.EndTag("programme"),
            filteredChannels = listOf("CCTV-1"),
        )

        assertEquals(1, epgList.size)
        assertEquals("CCTV-1", epgList[0].channel)
        assertTrue(epgList[0].programmes.isEmpty())
    }

    @Test
    fun `parse drops programme of unknown channel`() {
        val epgList = parse(
            Event.StartTag(
                "programme",
                mapOf(
                    "channel" to "unknown",
                    "start" to "20210907120000 +0800",
                    "stop" to "20210907130000 +0800",
                ),
            ),
            Event.StartTag("title"),
            Event.Text("孤儿节目"),
            Event.EndTag("title"),
            Event.EndTag("programme"),
        )

        assertTrue(epgList.isEmpty())
    }

    @Test
    fun `parse handles half hour timezone offset`() {
        val epgList = parse(
            Event.StartTag("channel", mapOf("id" to "cctv1")),
            Event.StartTag("display-name"),
            Event.Text("CCTV-1"),
            Event.EndTag("display-name"),
            Event.EndTag("channel"),
            Event.StartTag(
                "programme",
                mapOf(
                    "channel" to "cctv1",
                    "start" to "20210907120000 +0530",
                    "stop" to "20210907130000 +0530",
                ),
            ),
            Event.StartTag("title"),
            Event.Text("新闻联播"),
            Event.EndTag("title"),
            Event.EndTag("programme"),
        )

        val programme = epgList[0].programmes[0]
        assertEquals(3_600_000L, programme.endAt - programme.startAt)
    }

    // ---------- Fake XmlPullParser ----------

    private sealed interface Event {
        val type: Int

        data class StartTag(
            val name: String,
            val attributes: Map<String, String> = emptyMap(),
        ) : Event {
            override val type = XmlPullParser.START_TAG
        }

        data class EndTag(val name: String) : Event {
            override val type = XmlPullParser.END_TAG
        }

        data class Text(val value: String) : Event {
            override val type = XmlPullParser.TEXT
        }
    }

    private class FakeXmlPullParser(
        private val events: List<Event>,
    ) : XmlPullParser {
        private var index = 0

        private val current: Event get() = events[index]

        override fun getEventType(): Int =
            if (index >= events.size) XmlPullParser.END_DOCUMENT else current.type

        override fun next(): Int {
            if (index < events.size) index++
            return getEventType()
        }

        override fun nextTag(): Int {
            var type = next()
            while (type == XmlPullParser.TEXT && getText().isBlank()) {
                type = next()
            }
            if (type != XmlPullParser.START_TAG && type != XmlPullParser.END_TAG) {
                throw XmlPullParserException("expected START_TAG or END_TAG, got $type")
            }
            return type
        }

        override fun nextText(): String {
            if (getEventType() != XmlPullParser.START_TAG) {
                throw XmlPullParserException("parser must be on START_TAG to read next text")
            }
            return when (val type = next()) {
                XmlPullParser.TEXT -> {
                    val result = getText()
                    val end = next()
                    if (end != XmlPullParser.END_TAG) {
                        throw XmlPullParserException("expected END_TAG, got $end")
                    }
                    result
                }

                XmlPullParser.END_TAG -> ""

                else -> throw XmlPullParserException("expected TEXT or END_TAG, got $type")
            }
        }

        override fun getName(): String =
            when (val event = current) {
                is Event.StartTag -> event.name
                is Event.EndTag -> event.name
                else -> throw XmlPullParserException("no name at current position")
            }

        override fun getAttributeValue(namespace: String?, name: String?): String? =
            (current as? Event.StartTag)?.attributes?.get(name)

        override fun getText(): String = (current as? Event.Text)?.value ?: ""

        override fun isWhitespace(): Boolean = getText().isBlank()

        override fun getAttributeCount(): Int =
            (current as? Event.StartTag)?.attributes?.size ?: -1

        override fun getAttributeName(index: Int): String? =
            (current as? Event.StartTag)?.attributes?.keys?.elementAtOrNull(index)

        override fun getAttributeValue(index: Int): String? =
            (current as? Event.StartTag)?.attributes?.values?.elementAtOrNull(index)

        override fun setFeature(name: String, state: Boolean) = Unit

        override fun getFeature(name: String): Boolean = false

        override fun setProperty(name: String, value: Any?) = Unit

        override fun getProperty(name: String): Any? = null

        override fun setInput(input: Reader) = Unit

        override fun setInput(inputStream: InputStream, inputEncoding: String?) = Unit

        override fun getInputEncoding(): String? = null

        override fun defineEntityReplacementText(entityName: String, replacementText: String) = Unit

        override fun getDepth(): Int = 0

        override fun getPositionDescription(): String? = null

        override fun getLineNumber(): Int = -1

        override fun getColumnNumber(): Int = -1

        override fun getTextCharacters(holderForStartAndLength: IntArray?): CharArray =
            getText().toCharArray()

        override fun getNamespace(): String? = null

        override fun getNamespace(prefix: String): String? = null

        override fun getNamespaceCount(depth: Int): Int = 0

        override fun getNamespacePrefix(pos: Int): String? = null

        override fun getNamespaceUri(pos: Int): String? = null

        override fun getPrefix(): String? = null

        override fun getAttributeNamespace(index: Int): String? = null

        override fun getAttributePrefix(index: Int): String? = null

        override fun getAttributeType(index: Int): String? = null

        override fun isAttributeDefault(index: Int): Boolean = false

        override fun isEmptyElementTag(): Boolean = false

        override fun nextToken(): Int = next()

        override fun require(type: Int, namespace: String?, name: String?) = Unit
    }
}
