package app.partners.pnsa

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform