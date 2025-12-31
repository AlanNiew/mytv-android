package top.yogiczy.mytv.data.repositories.epg.fetcher

import okhttp3.Response

class XmlEpgFetcher : EpgFetcher {
    override fun isSupport(url: String): Boolean {
        // 提取URL路径部分，不包含查询参数和片段
        val path = url.substringBefore('?').substringBefore('#')
        return path.endsWith(".xml")
    }

    override fun fetch(response: Response): String {
        return response.body!!.string()
    }
}