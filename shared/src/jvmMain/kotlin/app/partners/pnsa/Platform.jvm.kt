package app.partners.pnsa

class JvmPlatform : Platform {
    override val name: String = "Desktop"
}

actual fun getPlatform(): Platform = JvmPlatform()
