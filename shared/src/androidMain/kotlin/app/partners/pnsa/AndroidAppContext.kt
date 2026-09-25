package app.partners.pnsa

import android.app.Application
import org.osmdroid.config.Configuration

object AndroidAppContext {
    lateinit var application: Application

    fun bind(app: Application) {
        application = app
        Configuration.getInstance().apply {
            userAgentValue = app.packageName
            osmdroidBasePath = app.cacheDir
            load(app, app.getSharedPreferences("osmdroid", Application.MODE_PRIVATE))
        }
    }
}
