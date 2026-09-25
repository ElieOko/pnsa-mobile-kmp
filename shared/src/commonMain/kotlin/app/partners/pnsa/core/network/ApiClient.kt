package app.partners.pnsa.core.network

import app.partners.pnsa.core.config.AppConfig
import app.partners.pnsa.core.session.SessionRepository
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.KSerializer

class ApiClient(
    private val session: SessionRepository,
    private val onUnauthorized: () -> Unit = { session.clear() },
) {
    val http = platformHttpClient {
        expectSuccess = false
        install(ContentNegotiation) {
            json(AppJson)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        defaultRequest {
            url(AppConfig.apiBaseUrl.trimEnd('/') + "/")
            header(HttpHeaders.Accept, "application/json")
            contentType(ContentType.Application.Json)
            session.currentToken()?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    suspend fun <T> get(
        path: String,
        serializer: KSerializer<T>,
        builder: HttpRequestBuilder.() -> Unit = {},
    ): T = execute(serializer) { http.get(path, builder) }

    suspend fun <T> post(
        path: String,
        serializer: KSerializer<T>,
        body: Any? = null,
        builder: HttpRequestBuilder.() -> Unit = {},
    ): T = execute(serializer) {
        http.post(path) {
            if (body != null) setBody(body)
            builder()
        }
    }

    suspend fun <T> patch(
        path: String,
        serializer: KSerializer<T>,
        body: Any? = null,
        builder: HttpRequestBuilder.() -> Unit = {},
    ): T = execute(serializer) {
        http.patch(path) {
            if (body != null) setBody(body)
            builder()
        }
    }

    suspend fun <T> put(
        path: String,
        serializer: KSerializer<T>,
        body: Any? = null,
        builder: HttpRequestBuilder.() -> Unit = {},
    ): T = execute(serializer) {
        http.put(path) {
            if (body != null) setBody(body)
            builder()
        }
    }

    suspend fun <T> delete(
        path: String,
        serializer: KSerializer<T>,
        builder: HttpRequestBuilder.() -> Unit = {},
    ): T = execute(serializer) { http.delete(path, builder) }

    private suspend fun <T> execute(
        serializer: KSerializer<T>,
        block: suspend () -> HttpResponse,
    ): T {
        val response = try {
            block()
        } catch (error: Exception) {
            throw ApiException(
                statusCode = 0,
                message = "Connexion indisponible. Vérifie ton réseau puis réessaie.",
            )
        }
        val text = response.bodyAsText()
        if (response.status.value == 401) {
            onUnauthorized()
            throw ApiException(401, "Session expirée. Reconnecte-toi pour continuer.")
        }
        if (!response.status.isSuccess()) {
            val parsed = runCatching { AppJson.decodeFromString(ValidationErrorDto.serializer(), text) }.getOrNull()
            throw ApiException(
                statusCode = response.status.value,
                message = parsed?.message ?: defaultMessage(response.status.value),
                fieldErrors = parsed?.errors.orEmpty(),
            )
        }
        if (text.isBlank()) {
            error("Réponse vide du serveur")
        }
        return AppJson.decodeFromString(serializer, text)
    }

    private fun defaultMessage(code: Int): String = when (code) {
        404 -> "Ressource introuvable."
        409 -> "Cette action n’est plus modifiable."
        422 -> "Certaines informations sont invalides."
        429 -> "Trop de tentatives. Réessaie dans un instant."
        in 500..599 -> "Le service est temporairement indisponible."
        else -> "Une erreur est survenue ($code)."
    }
}

fun HttpRequestBuilder.query(name: String, value: Any?) {
    if (value != null && value.toString().isNotBlank()) {
        parameter(name, value)
    }
}
