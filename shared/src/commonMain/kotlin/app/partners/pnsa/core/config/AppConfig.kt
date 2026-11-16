package app.partners.pnsa.core.config

object AppConfig {
    const val siteBaseUrl: String = "https://uxfqst-ip-167-86-108-98.tunnelmole.net"
    const val publicSiteUrl: String = "https://pnsa.cd"
    const val apiBaseUrl: String = "$siteBaseUrl/api"
    const val mediaBaseUrl: String = siteBaseUrl
    const val contactEmail: String = "secretariat@pnsa.cd"
    const val contactPhone: String = "+243 862 01 1506"
    const val contactPhoneTel: String = "+243862011506"
    const val privacyPolicyVersion: String = "1.0"
    const val termsVersion: String = "1.0"
    const val googleMapsApiKey: String = "AIzaSyBtJ_UIIACwrn6lDgoHYsoDaeBIsr-HKTE"

    /** Nom affiché de l’application. */
    const val appDisplayName: String = "Repère Jeunes"

    /** Accroche courte (accueil / stores). */
    const val appShortTagline: String =
        "Le bon service de santé, au bon endroit, près de toi !"

    /**
     * Petite note descriptive de l’application (Aide / À propos / fiches stores).
     * Informe sans promettre un acte médical.
     */
    const val appDescriptiveNote: String =
        "Repère Jeunes est l’application du Programme National de Santé de l’Adolescent (PNSA). " +
            "Elle t’aide à trouver un service SSR près de toi, à apprendre, à faire des quiz, " +
            "à échanger et à demander une orientation — en toute confidentialité. " +
            "Ce service informe et oriente : il ne remplace pas une consultation ni une urgence médicale."

    fun mediaUrl(path: String?): String? {
        val clean = path?.trim().orEmpty()
        if (clean.isEmpty()) return null
        if (clean.startsWith("http://") || clean.startsWith("https://")) return clean
        return "$mediaBaseUrl/${clean.trimStart('/')}"
    }
}
