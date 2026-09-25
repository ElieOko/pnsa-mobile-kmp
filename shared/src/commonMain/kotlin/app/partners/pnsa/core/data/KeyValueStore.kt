package app.partners.pnsa.core.data

interface KeyValueStore {
    fun get(key: String): String?
    fun put(key: String, value: String?)
}

expect fun createKeyValueStore(): KeyValueStore
