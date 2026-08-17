package top.yogiczy.mytv.data.repositories.epg

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import top.yogiczy.mytv.data.entities.Epg
import top.yogiczy.mytv.data.entities.EpgList
import top.yogiczy.mytv.data.entities.EpgProgramme
import top.yogiczy.mytv.data.entities.EpgProgrammeList
import top.yogiczy.mytv.utils.Logger
import java.io.StringReader

/**
 * 节目单 XML 解析
 *
 * 解析逻辑独立成纯 Kotlin 类,XmlPullParser 通过工厂注入,
 * 便于在 JVM 单元测试中替换为 Fake 实现。
 */
internal class EpgXmlParser(
    private val parserFactory: (String) -> XmlPullParser = { xmlString ->
        Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(StringReader(xmlString))
        }
    },
) {
    private val log = Logger.create(javaClass.simpleName)

    fun parse(
        xmlString: String,
        filteredChannels: List<String> = emptyList(),
    ): EpgList {
        val parser = parserFactory(xmlString)

        val epgMap = mutableMapOf<String, Epg>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "channel" -> {
                            val channelId = parser.getAttributeValue(null, "id")
                            parser.nextTag()
                            val channelName = parser.nextText()

                            if (filteredChannels.isEmpty() || filteredChannels.contains(channelName)) {
                                epgMap[channelId] = Epg(channelName, EpgProgrammeList())
                            }
                        }

                        "programme" -> {
                            val channelId = parser.getAttributeValue(null, "channel")
                            val startTime = parser.getAttributeValue(null, "start")
                            val stopTime = parser.getAttributeValue(null, "stop")
                            parser.nextTag()
                            val title = parser.nextText()

                            if (epgMap.containsKey(channelId)) {
                                val epg = epgMap.getValue(channelId)
                                epgMap[channelId] = epg.copy(
                                    programmes = EpgProgrammeList(
                                        epg.programmes + listOf(
                                            EpgProgramme(
                                                startAt = parseEpgTime(startTime),
                                                endAt = parseEpgTime(stopTime),
                                                title = title,
                                            )
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        log.i("解析节目单完成，共${epgMap.size}个频道")
        return EpgList(epgMap.values.toList())
    }
}
