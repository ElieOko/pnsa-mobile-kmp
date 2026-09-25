package app.partners.pnsa.core.data

import java.util.prefs.Preferences

actual fun createKeyValueStore(): KeyValueStore {
    val prefs = Preferences.userRoot().node("app.partners.pnsa")
    return object : KeyValueStore {
        override fun get(key: String): String? = prefs.get(key, null)
        override fun put(key: String, value: String?) {
            if (value == null) prefs.remove(key) else prefs.put(key, value)
        }
    }
}
