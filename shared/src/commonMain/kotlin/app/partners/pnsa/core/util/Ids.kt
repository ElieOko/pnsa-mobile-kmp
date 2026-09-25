package app.partners.pnsa.core.util

import kotlin.random.Random

fun randomUuid(): String {
    val bytes = ByteArray(16)
    Random.nextBytes(bytes)
    bytes[6] = (bytes[6].toInt() and 0x0f or 0x40).toByte()
    bytes[8] = (bytes[8].toInt() and 0x3f or 0x80).toByte()
    val hex = bytes.joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
    return "${hex.substring(0, 8)}-${hex.substring(8, 12)}-${hex.substring(12, 16)}-${hex.substring(16, 20)}-${hex.substring(20)}"
}

fun formatIsoDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    val cleaned = raw.replace('T', ' ').substringBefore('.')
    return cleaned.take(16)
}

fun excerpt(text: String?, max: Int = 140): String {
    val clean = text.orEmpty().replace("\r\n", "\n").trim()
    if (clean.length <= max) return clean
    return clean.take(max).trimEnd() + "…"
}

object DrcLocations {
    val provinces: List<String> = listOf(
        "Kinshasa",
        "Kongo Central",
        "Kwango",
        "Kwilu",
        "Mai-Ndombe",
        "Équateur",
        "Mongala",
        "Nord-Ubangi",
        "Sud-Ubangi",
        "Tshuapa",
        "Tshopo",
        "Bas-Uele",
        "Haut-Uele",
        "Ituri",
        "Nord-Kivu",
        "Sud-Kivu",
        "Maniema",
        "Haut-Katanga",
        "Lualaba",
        "Haut-Lomami",
        "Tanganyika",
        "Lomami",
        "Kasaï",
        "Kasaï-Central",
        "Kasaï-Oriental",
        "Sankuru",
    )
}
