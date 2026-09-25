package app.partners.pnsa.core.data

import platform.Foundation.NSUserDefaults

actual fun createKeyValueStore(): KeyValueStore {
    val defaults = NSUserDefaults.standardUserDefaults
    return object : KeyValueStore {
        override fun get(key: String): String? = defaults.stringForKey(key)
        override fun put(key: String, value: String?) {
            if (value == null) {
                defaults.removeObjectForKey(key)
            } else {
                defaults.setObject(value, key)
            }
        }
    }
}
