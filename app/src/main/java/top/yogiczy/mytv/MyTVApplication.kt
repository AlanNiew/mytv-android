package top.yogiczy.mytv

import android.app.Application
import top.yogiczy.mytv.ui.utils.SP

class MyTVApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AppGlobal.cacheDir = applicationContext.cacheDir
        SP.init(applicationContext)

        // 信任所有证书默认关闭,仅当用户在设置中显式开启后生效
        if (SP.httpTrustAllCertificates) {
            UnsafeTrustManager.enableUnsafeTrustManager()
        }
    }
}
