package app.partners.pnsa.core.session

import app.partners.pnsa.core.data.KeyValueStore
import app.partners.pnsa.core.network.AppJson
import app.partners.pnsa.features.user.domain.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString

class SessionRepository(
    private val store: KeyValueStore,
) {
    private val _token = MutableStateFlow(store.get(KEY_TOKEN))
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _user = MutableStateFlow(readUser())
    val user: StateFlow<User?> = _user.asStateFlow()

    val isSignedIn: Boolean get() = !_token.value.isNullOrBlank()

    fun currentToken(): String? = _token.value

    fun save(token: String, user: User) {
        store.put(KEY_TOKEN, token)
        store.put(KEY_USER, AppJson.encodeToString(user))
        _token.value = token
        _user.value = user
    }

    fun updateUser(user: User) {
        store.put(KEY_USER, AppJson.encodeToString(user))
        _user.value = user
    }

    fun clear() {
        store.put(KEY_TOKEN, null)
        store.put(KEY_USER, null)
        store.put(KEY_SYNC, null)
        _token.value = null
        _user.value = null
    }

    fun saveLastSync(iso: String?) {
        store.put(KEY_SYNC, iso)
    }

    fun lastSync(): String? = store.get(KEY_SYNC)

    private fun readUser(): User? {
        val raw = store.get(KEY_USER) ?: return null
        return runCatching { AppJson.decodeFromString<User>(raw) }.getOrNull()
    }

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER = "auth_user"
        private const val KEY_SYNC = "last_sync"
    }
}
