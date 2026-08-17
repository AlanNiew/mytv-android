package top.yogiczy.mytv.data.repositories.git.parser

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import top.yogiczy.mytv.data.entities.GitRelease

class GiteeGitReleaseParser : GitReleaseParser {
    override fun isSupport(url: String): Boolean {
        return url.contains("gitee.com")
    }

    override suspend fun parse(data: String): GitRelease {
        val json = Json.parseToJsonElement(data).jsonObject

        val downloadUrl = (json["assets"] as? JsonArray).orEmpty()
            .firstNotNullOfOrNull { asset ->
                (asset.jsonObject["browser_download_url"] as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content
            } ?: ""

        return GitRelease(
            // tag 可能不带 v 前缀,统一 removePrefix 处理
            version = (json["tag_name"] as? JsonPrimitive)?.takeIf { it !is JsonNull }
                ?.content?.removePrefix("v") ?: "",
            downloadUrl = downloadUrl,
            description = (json["body"] as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content ?: "",
        )
    }
}
