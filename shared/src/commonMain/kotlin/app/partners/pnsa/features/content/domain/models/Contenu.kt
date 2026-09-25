package app.partners.pnsa.features.content.domain.models

import app.partners.pnsa.core.network.NullableFlexibleBoolSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Contenu(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    val description: String? = null,
    val thumbnail: String? = null,
    @SerialName("thumbnail_path") val thumbnailPath: String? = null,
    val file: String? = null,
    @SerialName("file_path") val filePath: String? = null,
    val extension: String? = null,
    val likes: String? = null,
    val unlikes: String? = null,
    val vieweds: String? = null,
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val status: Boolean? = true,
    @SerialName("status_del")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val statusDel: Boolean? = true,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("type_contenu_id") val typeContenuId: Long? = null,
    @SerialName("content_version") val contentVersion: Int? = null,
    val langue: String? = null,
    val theme: String? = null,
    @SerialName("age_min") val ageMin: Int? = null,
    @SerialName("age_max") val ageMax: Int? = null,
    @SerialName("allow_offline")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val allowOffline: Boolean? = null,
    @SerialName("editorial_status") val editorialStatus: String? = null,
    @SerialName("archived_at") val archivedAt: String? = null,
    @SerialName("type_contenu") val typeContenu: TypeContenu? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    val title: String get() = libelle?.ifBlank { null } ?: "Contenu"
    val categoryLabel: String get() = typeContenu?.libelle?.ifBlank { null } ?: theme ?: "Ressource"
    val excerpt: String get() = description.orEmpty().replace("\r\n", "\n").trim().take(160)
}

@Serializable
data class TypeContenu(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    val image: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
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
data class Actualite(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    val description: String? = null,
    val file: String? = null,
    @SerialName("file_path") val filePath: String? = null,
    val extension: String? = null,
    val thumbnail: String? = null,
    @SerialName("thumbnail_path") val thumbnailPath: String? = null,
    val likes: String? = null,
    val unlikes: String? = null,
    val vieweds: String? = null,
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val status: Boolean? = true,
    @SerialName("status_del")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val statusDel: Boolean? = true,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("type_contenu_id") val typeContenuId: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class Faq(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    val description: String? = null,
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
data class HomeFeed(
    val contenus: List<Contenu> = emptyList(),
    val actualites: List<Actualite> = emptyList(),
    val faqs: List<Faq> = emptyList(),
)

@Serializable
data class CatalogSyncPayload(
    val contenus: List<Contenu> = emptyList(),
    val quizzes: List<app.partners.pnsa.features.quiz.domain.models.Quiz> = emptyList(),
    val actualites: List<Actualite> = emptyList(),
    val faqs: List<Faq> = emptyList(),
    val categories: List<app.partners.pnsa.features.forum.domain.models.Categorie> = emptyList(),
    @SerialName("type_contenus") val typeContenus: List<TypeContenu> = emptyList(),
    val cursor: String? = null,
    @SerialName("synced_at") val syncedAt: String? = null,
)
