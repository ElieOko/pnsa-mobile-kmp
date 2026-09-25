package app.partners.pnsa.core.config

object AppConfig {
    const val siteBaseUrl: String = "https://uxfqst-ip-167-86-108-98.tunnelmole.net"
    const val apiBaseUrl: String = "$siteBaseUrl/api"
    const val mediaBaseUrl: String = siteBaseUrl
    const val contactEmail: String = "secretariat@pnsa.cd"
    const val contactPhone: String = "+243 862 01 1506"
    const val contactPhoneTel: String = "+243862011506"
    const val privacyPolicyVersion: String = "1.0"
    const val termsVersion: String = "1.0"

    fun mediaUrl(path: String?): String? {
        val clean = path?.trim().orEmpty()
        if (clean.isEmpty()) return null
        if (clean.startsWith("http://") || clean.startsWith("https://")) return clean
        return "$mediaBaseUrl/${clean.trimStart('/')}"
    }
}
