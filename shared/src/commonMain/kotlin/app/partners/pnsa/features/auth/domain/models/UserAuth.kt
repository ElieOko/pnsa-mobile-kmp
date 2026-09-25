package app.partners.pnsa.features.auth.domain.models

import app.partners.pnsa.features.user.domain.models.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class UserAuth(
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val nom: String,
    val prenom: String,
    val email: String,
    val password: String,
    @SerialName("password_confirmation") val passwordConfirmation: String,
    val genre: String,
    @SerialName("date_naissance") val dateNaissance: String,
    val phone: String? = null,
    val province: String? = null,
    val ville: String? = null,
    val langue: String = "fr",
    val consents: List<ConsentPayload> = emptyList(),
)

@Serializable
data class ConsentPayload(
    val type: String,
    val granted: Boolean,
    @SerialName("policy_version") val policyVersion: String? = null,
)

@Serializable
data class AuthTokenResponse(
    val token: String? = null,
    @SerialName("token_type") val tokenType: String? = null,
    val user: User? = null,
)

@Serializable
data class ProfileUpdateRequest(
    val nom: String? = null,
    val prenom: String? = null,
    val phone: String? = null,
    val province: String? = null,
    val ville: String? = null,
    val langue: String? = null,
    @SerialName("lieu_residence") val lieuResidence: String? = null,
)

@Serializable
data class ConsentsUpdateRequest(
    val consents: List<ConsentPayload>,
)

@Serializable
data class NotificationPreferencesUpdate(
    @SerialName("push_enabled") val pushEnabled: Boolean? = null,
    @SerialName("forum_enabled") val forumEnabled: Boolean? = null,
    @SerialName("advice_enabled") val adviceEnabled: Boolean? = null,
    @SerialName("orientation_enabled") val orientationEnabled: Boolean? = null,
)

@Serializable
data class CreateCommentRequest(
    val commentaire: String,
)

@Serializable
data class CreateMessageRequest(
    val msg: String,
)
