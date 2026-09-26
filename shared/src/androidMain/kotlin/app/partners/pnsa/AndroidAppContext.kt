package app.partners.pnsa

import android.app.Application

object AndroidAppContext {
    lateinit var application: Application

    val isBound: Boolean get() = ::application.isInitialized

    fun bind(app: Application) {
        application = app
    }
}
