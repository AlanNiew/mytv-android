package top.yogiczy.mytv.ui.utils

import android.content.Context
import android.widget.Toast
import com.koushikdutta.async.AsyncServer
import com.koushikdutta.async.http.body.JSONObjectBody
import com.koushikdutta.async.http.body.MultipartFormDataBody
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.AsyncHttpServerRequest
import com.koushikdutta.async.http.server.AsyncHttpServerResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import top.yogiczy.mytv.AppGlobal
import top.yogiczy.mytv.R
import top.yogiczy.mytv.data.repositories.epg.EpgRepository
import top.yogiczy.mytv.data.repositories.iptv.IptvRepository
import top.yogiczy.mytv.data.utils.Constants
import top.yogiczy.mytv.utils.ApkInstaller
import top.yogiczy.mytv.utils.Loggable
import top.yogiczy.mytv.utils.Logger
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException

object HttpServer : Loggable() {
    private const val SERVER_PORT = 10481

    /** 上传 APK 大小上限(200MB) */
    private const val MAX_APK_SIZE = 200L * 1024 * 1024

    /** 鉴权请求头,与网页端 requestApi 保持一致 */
    private const val AUTH_HEADER = "X-Auth-Token"

    private val uploadedApkFile = File(AppGlobal.cacheDir, "uploaded_apk.apk").apply {
        deleteOnExit()
    }

    private var showToast: (String) -> Unit = { }

    val serverUrl: String by lazy {
        "http://${getLocalIpAddress()}:${SERVER_PORT}"
    }

    fun start(context: Context, showToast: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val server = AsyncHttpServer()
                server.listen(AsyncServer.getDefault(), SERVER_PORT)

                server.get("/") { _, response ->
                    handleRawResource(response, context, "text/html", R.raw.index)
                }
                server.get("/index_css.css") { _, response ->
                    handleRawResource(response, context, "text/css", R.raw.index_css)
                }
                server.get("/index_js.js") { _, response ->
                    handleRawResource(response, context, "text/javascript", R.raw.index_js)
                }

                server.get("/api/settings") { request, response ->
                    handleGetSettings(request, response)
                }

                server.post("/api/settings") { request, response ->
                    handleSetSettings(request, response)
                }

                server.post("/api/upload/apk") { request, response ->
                    handleUploadApk(request, response, context)
                }

                HttpServer.showToast = showToast
                log.i("服务已启动: 0.0.0.0:${SERVER_PORT}")
            } catch (ex: Exception) {
                log.e("服务启动失败: ${ex.message}", ex)
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.http_server_start_failed),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    private fun wrapResponse(response: AsyncHttpServerResponse) = response.apply {
        headers.set(
            "Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, OPTIONS"
        )
        headers.set("Access-Control-Allow-Origin", "*")
        headers.set(
            "Access-Control-Allow-Headers", "Origin, Content-Type, X-Auth-Token"
        )
    }

    /**
     * 鉴权校验,失败时返回 401
     * @return 是否通过校验
     */
    private fun checkAuth(
        request: AsyncHttpServerRequest,
        response: AsyncHttpServerResponse,
    ): Boolean {
        val token = request.headers[AUTH_HEADER] ?: ""
        if (token == SP.httpToken) return true

        log.w("鉴权失败: 请求缺少或携带错误令牌")
        wrapResponse(response).code(401).send("unauthorized")
        return false
    }

    private fun handleRawResource(
        response: AsyncHttpServerResponse,
        context: Context,
        contentType: String,
        id: Int,
    ) {
        wrapResponse(response).apply {
            setContentType(contentType)
            send(context.resources.openRawResource(id).readBytes().decodeToString())
        }
    }

    private fun handleGetSettings(
        request: AsyncHttpServerRequest,
        response: AsyncHttpServerResponse,
    ) {
        // 配置与日志含订阅源地址等隐私信息,读取同样需要鉴权
        if (!checkAuth(request, response)) return

        wrapResponse(response).apply {
            setContentType("application/json")
            send(
                Json.encodeToString(
                    AllSettings(
                        appTitle = Constants.APP_TITLE,
                        appRepo = Constants.APP_REPO,
                        iptvSourceUrl = SP.iptvSourceUrl,
                        epgXmlUrl = SP.epgXmlUrl,
                        videoPlayerUserAgent = SP.videoPlayerUserAgent,
                        logHistory = Logger.history,
                    )
                )
            )
        }
    }

    private fun handleSetSettings(
        request: AsyncHttpServerRequest,
        response: AsyncHttpServerResponse,
    ) {
        if (!checkAuth(request, response)) return

        val body = request.getBody<JSONObjectBody>().get()
        val iptvSourceUrl = body.get("iptvSourceUrl").toString()
        val epgXmlUrl = body.get("epgXmlUrl").toString()
        val videoPlayerUserAgent = body.get("videoPlayerUserAgent").toString()

        if (SP.iptvSourceUrl != iptvSourceUrl) {
            SP.iptvSourceUrl = iptvSourceUrl
            IptvRepository().clearCache()
        }

        if (SP.epgXmlUrl != epgXmlUrl) {
            SP.epgXmlUrl = epgXmlUrl
            EpgRepository().clearCache()
        }

        SP.videoPlayerUserAgent = videoPlayerUserAgent

        wrapResponse(response).send("success")
    }

    private fun handleUploadApk(
        request: AsyncHttpServerRequest,
        response: AsyncHttpServerResponse,
        context: Context,
    ) {
        if (!checkAuth(request, response)) return

        // 根据 Content-Length 提前拒绝超大文件
        val contentLength = request.headers["Content-Length"]?.toLong() ?: -1
        if (contentLength > MAX_APK_SIZE) {
            log.w("拒绝上传: 文件过大($contentLength)")
            wrapResponse(response).code(413).send("file too large")
            return
        }

        val body = request.getBody<MultipartFormDataBody>()

        val os = uploadedApkFile.outputStream()
        var hasReceived = 0L
        var hasError = false

        body.setMultipartCallback { part ->
            if (part.isFile) {
                body.setDataCallback { _, bb ->
                    val byteArray = bb.allByteArray
                    hasReceived += byteArray.size
                    if (hasReceived > MAX_APK_SIZE) {
                        hasError = true
                        log.w("拒绝上传: 超出大小上限")
                        body.dataEmitter.close()
                        return@setDataCallback
                    }
                    val percent = if (contentLength > 0) {
                        (hasReceived * 100f / contentLength).toInt()
                    } else {
                        0
                    }
                    showToast(context.getString(R.string.http_upload_progress, percent))
                    os.write(byteArray)
                }
            }
        }

        body.setEndCallback {
            try {
                os.flush()
            } catch (ex: Exception) {
                log.e("上传APK写入失败", ex)
                hasError = true
            } finally {
                try {
                    os.close()
                } catch (_: Exception) {
                }
            }

            if (!hasError && hasReceived > 0) {
                showToast(context.getString(R.string.http_upload_complete))
                ApkInstaller.installApk(context, uploadedApkFile.path)
            } else {
                log.w("上传APK失败: 文件为空或接收异常")
                uploadedApkFile.delete()
            }
        }

        wrapResponse(response).send("success")
    }

    private fun getLocalIpAddress(): String {
        val defaultIp = "0.0.0.0"

        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.hostAddress ?: defaultIp
                    }
                }
            }
            return defaultIp
        } catch (ex: SocketException) {
            log.e("IP Address: ${ex.message}", ex)
            return defaultIp
        }
    }
}

@Serializable
private data class AllSettings(
    val appTitle: String,
    val appRepo: String,
    val iptvSourceUrl: String,
    val epgXmlUrl: String,
    val videoPlayerUserAgent: String,

    val logHistory: List<Logger.HistoryItem>,
)