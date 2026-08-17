package top.yogiczy.mytv.data.repositories.epg

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Request
import top.yogiczy.mytv.data.OkHttpClientProvider
import top.yogiczy.mytv.data.entities.Epg
import top.yogiczy.mytv.data.entities.EpgList
import top.yogiczy.mytv.data.repositories.FileCacheRepository
import top.yogiczy.mytv.data.repositories.epg.fetcher.EpgFetcher
import top.yogiczy.mytv.utils.Logger
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 节目单获取
 */
class EpgRepository : FileCacheRepository("epg.json") {
    private val log = Logger.create(javaClass.simpleName)
    private val epgXmlRepository = EpgXmlRepository()
    private val epgXmlParser = EpgXmlParser()

    suspend fun getEpgList(
        xmlUrl: String,
        filteredChannels: List<String> = emptyList(),
        refreshTimeThreshold: Int,
    ) = withContext(Dispatchers.Default) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // 未到刷新时间点(如凌晨0-2点)时沿用缓存,避免节目单整段为空;
            // 无缓存时仍会触发首次拉取
            val xmlJson = getOrRefresh({ lastModified, _ ->
                val now = System.currentTimeMillis()
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                hour >= refreshTimeThreshold &&
                    dateFormat.format(now) != dateFormat.format(lastModified)
            }) {
                val xmlString = epgXmlRepository.getEpgXml(xmlUrl)
                Json.encodeToString(epgXmlParser.parse(xmlString, filteredChannels).value)
            }

            EpgList(Json.decodeFromString<List<Epg>>(xmlJson))
        } catch (ex: Exception) {
            log.e("获取节目单失败", ex)
            throw Exception(ex)
        }
    }
}

/**
 * 节目单xml获取
 */
private class EpgXmlRepository : FileCacheRepository("epg.xml") {
    private val log = Logger.create(javaClass.simpleName)

    /**
     * 获取远程xml
     */
    private suspend fun fetchXml(url: String): String = withContext(Dispatchers.IO) {
        log.d("获取远程节目单xml: $url")

        val request = Request.Builder().url(url).build()

        try {
            OkHttpClientProvider.client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("获取远程节目单xml失败: ${response.code}")
                }

                val fetcher = EpgFetcher.instances.first { it.isSupport(url) }

                return@withContext fetcher.fetch(response)
            }
        } catch (ex: Exception) {
            throw Exception("获取远程节目单xml失败，请检查网络连接", ex)
        }
    }

    /**
     * 获取xml
     */
    suspend fun getEpgXml(url: String): String {
        return getOrRefresh(0) {
            fetchXml(url)
        }
    }
}
