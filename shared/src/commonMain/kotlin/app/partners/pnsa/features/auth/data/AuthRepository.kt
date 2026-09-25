package app.partners.pnsa.features.auth.data

import app.partners.pnsa.core.network.ApiClient
import app.partners.pnsa.core.session.SessionRepository
import app.partners.pnsa.features.auth.domain.models.AuthTokenResponse
import app.partners.pnsa.features.auth.domain.models.LoginRequest
import app.partners.pnsa.features.auth.domain.models.RegisterRequest
import app.partners.pnsa.features.user.domain.models.User

class AuthRepository(
    private val api: ApiClient,
    private val session: SessionRepository,
) {
    suspend fun login(email: String, password: String): User {
        val response = api.post(
            path = "auth/login",
            serializer = AuthTokenResponse.serializer(),
            body = LoginRequest(email = email.trim(), password = password),
        )
        persist(response)
        return response.user ?: fetchMe()
    }

    suspend fun register(request: RegisterRequest): User {
        val response = api.post(
            path = "auth/register",
            serializer = AuthTokenResponse.serializer(),
            body = request,
        )
        persist(response)
        return response.user ?: fetchMe()
    }

    suspend fun fetchMe(): User {
        val user = api.get("auth/me", User.serializer())
        session.updateUser(user)
        return user
    }

    suspend fun logout() {
        runCatching {
            api.post("auth/logout", app.partners.pnsa.core.network.MessageDto.serializer(), body = null)
        }
        session.clear()
    }

    private fun persist(response: AuthTokenResponse) {
        val token = response.token?.takeIf { it.isNotBlank() }
            ?: error("Le serveur n’a pas renvoyé de jeton.")
        val user = response.user ?: User(email = null)
        session.save(token, user)
    }
}
