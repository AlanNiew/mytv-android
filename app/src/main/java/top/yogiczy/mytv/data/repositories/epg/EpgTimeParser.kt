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
        // 设置时区偏移;无法识别的偏移不静默按 GMT 处理
        if (timezonePart.isNotEmpty()) {
            val timeZone = parseTimeZoneOffset(timezonePart) ?: return 0
            sdf.timeZone = timeZone
        }

        sdf.parse(dateTimePart)?.time ?: 0
    } catch (e: Exception) {
        0
    }
}

/**
 * 解析 "+0800"、"+08:00"、"+0830"、"+5:30" 等时区偏移
 * @return 对应的 TimeZone,无法解析时返回 null
 */
private fun parseTimeZoneOffset(part: String): TimeZone? {
    val match = Regex("^([+-])(\\d{1,2}):?(\\d{2})$").find(part) ?: return null
    val sign = if (match.groupValues[1] == "-") -1 else 1
    val hours = match.groupValues[2].toIntOrNull() ?: return null
    val minutes = match.groupValues[3].toIntOrNull() ?: return null
    if (hours > 23 || minutes > 59) return null

    val offsetMs = sign * (hours * 60 + minutes) * 60_000L
    val timeZone = TimeZone.getTimeZone("GMT${match.groupValues[1]}%02d:%02d".format(hours, minutes))
    // TimeZone.getTimeZone 对非法 id 会静默返回 GMT,用偏移量校验结果
    return timeZone.takeIf { it.getOffset(0L) == offsetMs.toInt() }
}
