package app.partners.pnsa.features.user.domain.models

import app.partners.pnsa.core.network.FlexibleBoolSerializer
import app.partners.pnsa.core.network.NullableFlexibleBoolSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long? = null,
    val nom: String? = null,
    val prenom: String? = null,
    val phone: String? = null,
    val matricule: String? = null,
    @SerialName("date_naissance") val dateNaissance: String? = null,
    @SerialName("lieu_naissance") val lieuNaissance: String? = null,
    @SerialName("lieu_residence") val lieuResidence: String? = null,
    val email: String? = null,
    @SerialName("type_user_id") val typeUserId: Long? = null,
    val type: String? = null,
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val status: Boolean? = true,
    @SerialName("status_del")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val statusDel: Boolean? = true,
    @SerialName("email_verified_at") val emailVerifiedAt: String? = null,
    val genre: String? = null,
    val province: String? = null,
    val ville: String? = null,
    val langue: String? = null,
    @SerialName("type_user") val typeUser: TypeUser? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    val displayName: String
        get() = listOfNotNull(prenom, nom).joinToString(" ").ifBlank { email ?: "Utilisateur" }

    val initials: String
        get() = listOfNotNull(prenom?.firstOrNull(), nom?.firstOrNull())
            .joinToString("")
            .uppercase()
            .ifBlank { "?" }
}

@Serializable
data class TypeUser(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val status: Boolean? = true,
    @SerialName("status_del")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val statusDel: Boolean? = true,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class UserConsent(
    val id: Long? = null,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("consent_type") val consentType: String? = null,
    @Serializable(with = FlexibleBoolSerializer::class)
    val granted: Boolean = false,
    @SerialName("policy_version") val policyVersion: String? = null,
    @SerialName("granted_at") val grantedAt: String? = null,
    @SerialName("revoked_at") val revokedAt: String? = null,
)

@Serializable
data class NotificationPreferences(
    val id: Long? = null,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("push_enabled")
    @Serializable(with = FlexibleBoolSerializer::class)
    val pushEnabled: Boolean = true,
    @SerialName("forum_enabled")
    @Serializable(with = FlexibleBoolSerializer::class)
    val forumEnabled: Boolean = true,
    @SerialName("advice_enabled")
    @Serializable(with = FlexibleBoolSerializer::class)
    val adviceEnabled: Boolean = true,
    @SerialName("orientation_enabled")
    @Serializable(with = FlexibleBoolSerializer::class)
    val orientationEnabled: Boolean = true,
)

@Serializable
data class ProfileBundle(
    val user: User? = null,
    val consents: List<UserConsent> = emptyList(),
    @SerialName("notification_preferences")
    val notificationPreferences: NotificationPreferences? = null,
)
