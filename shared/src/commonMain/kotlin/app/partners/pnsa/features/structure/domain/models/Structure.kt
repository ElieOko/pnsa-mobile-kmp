package app.partners.pnsa.features.structure.domain.models

import app.partners.pnsa.core.network.NullableFlexibleBoolSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthStructure(
    val id: Long? = null,
    val name: String? = null,
    val nom: String? = null,
    val province: String? = null,
    val city: String? = null,
    val ville: String? = null,
    val address: String? = null,
    val adresse: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null,
    val email: String? = null,
    val services: String? = null,
    @SerialName("structure_type") val structureType: String? = null,
    @SerialName("opening_hours") val openingHours: String? = null,
    @SerialName("content_version") val contentVersion: Int? = null,
    val status: String? = null,
) {
    val displayName: String get() = name?.ifBlank { null } ?: nom ?: "Structure SSR"
    val displayCity: String get() = city?.ifBlank { null } ?: ville ?: "—"
    val displayAddress: String get() = address?.ifBlank { null } ?: adresse ?: "Adresse non renseignée"
    val hasCoordinates: Boolean get() = latitude != null && longitude != null && latitude != 0.0 && longitude != 0.0
}

@Serializable
data class StructureSyncPayload(
    val structures: List<HealthStructure> = emptyList(),
    val cursor: String? = null,
    @SerialName("synced_at") val syncedAt: String? = null,
)

@Serializable
data class Orientation(
    val id: Long? = null,
    val status: String? = null,
    val reason: String? = null,
    @SerialName("client_request_id") val clientRequestId: String? = null,
    @SerialName("health_structure") val healthStructure: HealthStructure? = null,
    @SerialName("health_structure_id") val healthStructureId: Long? = null,
    val events: List<OrientationEvent> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    val statusLabel: String
        get() = when (status) {
            "created" -> "Créée"
            "accepted" -> "Acceptée"
            "rejected" -> "Refusée"
            "in_progress" -> "En cours"
            "completed" -> "Réalisée"
            "cancelled" -> "Clôturée"
            else -> status ?: "—"
        }
}

@Serializable
data class OrientationEvent(
    val status: String? = null,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class OrientationCreateRequest(
    @SerialName("health_structure_id") val healthStructureId: Long,
    @SerialName("client_request_id") val clientRequestId: String,
    val reason: String? = null,
)

@Serializable
data class Activite(
    val id: Long? = null,
    val nom: String? = null,
    @SerialName("date_debut") val dateDebut: String? = null,
    @SerialName("date_fin") val dateFin: String? = null,
    val mois: Int? = null,
    val trimestre: Int? = null,
    val lieu: String? = null,
    @SerialName("structure_responsable") val structureResponsable: String? = null,
    @SerialName("autre_service") val autreService: String? = null,
    @SerialName("ligne_budgetaire") val ligneBudgetaire: Int? = null,
    val montant: Int? = null,
    @SerialName("be_ptf") val bePtf: String? = null,
    @SerialName("nom_raporteur") val nomRaporteur: String? = null,
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val status: Boolean? = true,
    @SerialName("status_del")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val statusDel: Boolean? = true,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)
