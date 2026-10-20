package app.partners.pnsa.core.data

import android.content.Context
import app.partners.pnsa.AndroidAppContext

actual fun createKeyValueStore(): KeyValueStore {
    if (!AndroidAppContext.isBound) {
        val memory = mutableMapOf<String, String>()
        return object : KeyValueStore {
            override fun get(key: String): String? = memory[key]
            override fun put(key: String, value: String?) {
                if (value == null) memory.remove(key) else memory[key] = value
            }
        }
    }
    val prefs = AndroidAppContext.application.getSharedPreferences("pnsa_session", Context.MODE_PRIVATE)
    return object : KeyValueStore {
        override fun get(key: String): String? = prefs.getString(key, null)
        override fun put(key: String, value: String?) {
            prefs.edit().apply {
                if (value == null) remove(key) else putString(key, value)
            }.apply()
        }
    }
}
