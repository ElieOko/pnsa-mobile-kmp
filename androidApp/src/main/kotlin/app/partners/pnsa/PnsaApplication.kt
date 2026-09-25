package app.partners.pnsa

import android.app.Application

class PnsaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidAppContext.bind(this)
    }
}
