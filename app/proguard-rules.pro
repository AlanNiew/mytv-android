# ==================== kotlinx.serialization ====================
# 序列化生成的 $serializer 类与 Companion.serializer() 需要保留,否则 release 反序列化崩溃
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class top.yogiczy.mytv.**$$serializer { *; }
-keepclassmembers class top.yogiczy.mytv.** {
    *** Companion;
}
-keepclasseswithmembers class top.yogiczy.mytv.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ==================== androidasync(内嵌 HTTP 设置服务器) ====================
# AsyncHttpServer 通过反射注册路由回调
-keep class com.koushikdutta.async.** { *; }
-dontwarn com.koushikdutta.async.**

# ==================== Media3 扩展渲染器(ffmpeg 软解,反射加载) ====================
-keep class androidx.media3.decoder.** { *; }

# ==================== 调试信息 ====================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
