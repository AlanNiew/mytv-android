package top.yogiczy.mytv.data

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object OkHttpClientProvider {
    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}
