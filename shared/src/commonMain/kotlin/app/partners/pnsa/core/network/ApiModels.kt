package app.partners.pnsa.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DataWrapper<T>(
    val data: T,
)

@Serializable
data class PageDto<T>(
    val data: List<T> = emptyList(),
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
    @SerialName("per_page") val perPage: Int = 12,
    val total: Int = 0,
    @SerialName("next_page_url") val nextPageUrl: String? = null,
)

@Serializable
data class ValidationErrorDto(
    val message: String? = null,
    val errors: Map<String, List<String>> = emptyMap(),
)

@Serializable
data class MessageDto(
    val message: String? = null,
)

@Serializable
data class AnalyticsBatchRequest(
    val events: List<AnalyticsEventDto>,
)

@Serializable
data class AnalyticsEventDto(
    @SerialName("event_type") val eventType: String,
    val payload: JsonObject? = null,
    @SerialName("occurred_at") val occurredAt: String? = null,
    @SerialName("session_id") val sessionId: String? = null,
)
