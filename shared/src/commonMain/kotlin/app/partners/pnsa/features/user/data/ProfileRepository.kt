package app.partners.pnsa.features.user.data

import app.partners.pnsa.core.network.AnalyticsBatchRequest
import app.partners.pnsa.core.network.AnalyticsEventDto
import app.partners.pnsa.core.network.ApiClient
import app.partners.pnsa.core.network.DataWrapper
import app.partners.pnsa.core.session.SessionRepository
import app.partners.pnsa.features.auth.domain.models.ConsentsUpdateRequest
import app.partners.pnsa.features.auth.domain.models.ConsentPayload
import app.partners.pnsa.features.auth.domain.models.NotificationPreferencesUpdate
import app.partners.pnsa.features.auth.domain.models.ProfileUpdateRequest
import app.partners.pnsa.features.user.domain.models.NotificationPreferences
import app.partners.pnsa.features.user.domain.models.ProfileBundle
import app.partners.pnsa.features.user.domain.models.User
import app.partners.pnsa.features.user.domain.models.UserConsent
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class ProfileRepository(
    private val api: ApiClient,
    private val session: SessionRepository,
) {
    suspend fun bundle(): ProfileBundle {
        val bundle = api.get("mobile/v1/profile", DataWrapper.serializer(ProfileBundle.serializer())).data
        bundle.user?.let { session.updateUser(it) }
        return bundle
    }

    suspend fun update(request: ProfileUpdateRequest): User {
        val user = api.patch(
            path = "mobile/v1/profile",
            serializer = DataWrapper.serializer(User.serializer()),
            body = request,
        ).data
        session.updateUser(user)
        return user
    }

    suspend fun updateConsents(consents: List<ConsentPayload>): List<UserConsent> {
        return api.put(
            path = "mobile/v1/profile/consents",
            serializer = DataWrapper.serializer(ListSerializer(UserConsent.serializer())),
            body = ConsentsUpdateRequest(consents),
        ).data
    }

    suspend fun updateNotifications(update: NotificationPreferencesUpdate): NotificationPreferences {
        return api.put(
            path = "mobile/v1/profile/notification-preferences",
            serializer = DataWrapper.serializer(NotificationPreferences.serializer()),
            body = update,
        ).data
    }

    suspend fun track(eventType: String, payload: Map<String, String> = emptyMap()) {
        runCatching {
            api.post(
                path = "mobile/v1/analytics/events",
                serializer = DataWrapper.serializer(JsonObject.serializer()),
                body = AnalyticsBatchRequest(
                    events = listOf(
                        AnalyticsEventDto(
                            eventType = eventType,
                            payload = JsonObject(payload.mapValues { JsonPrimitive(it.value) }),
                        ),
                    ),
                ),
            )
        }
    }
}
