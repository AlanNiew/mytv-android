package top.yogiczy.mytv.data.repositories.epg

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * 解析节目单时间字符串
 * 支持 "yyyyMMddHHmmss" 与 "yyyyMMddHHmmss +0800" 两种格式
 * @return epoch 毫秒,解析失败返回 0
 */
internal fun parseEpgTime(time: String): Long {
    if (time.length < 14) return 0

    return try {
        // 处理 "20251230124000 +0800" 格式
        val dateTimePart = time.substring(0, 14)  // "20251230124000"
        val timezonePart = time.substring(14).trim()  // "+0800"

        // 固定使用 US locale,避免部分系统 locale(如 tr_TR)下数字解析异常
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
        // 设置时区偏移
        if (timezonePart.isNotEmpty()) {
            sdf.timeZone = TimeZone.getTimeZone("GMT$timezonePart")
        }

        sdf.parse(dateTimePart)?.time ?: 0
    } catch (e: Exception) {
        0
    }
}
