package app.partners.pnsa.features.forum.domain.models

import app.partners.pnsa.core.network.NullableFlexibleBoolSerializer
import app.partners.pnsa.features.user.domain.models.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Categorie(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    val description: String? = null,
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
data class Sujet(
    val id: Long? = null,
    val code: String? = null,
    val title: String? = null,
    val libelle: String? = null,
    val description: String? = null,
    val image: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    val likes: String? = null,
    val unlikes: String? = null,
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val status: Boolean? = true,
    @SerialName("status_del")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val statusDel: Boolean? = true,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("categorie_id") val categorieId: Long? = null,
    val categorie: Categorie? = null,
    val user: User? = null,
    val commentaires: List<Commentaire> = emptyList(),
    @SerialName("commentaires_count") val commentairesCount: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    val headline: String get() = title?.ifBlank { null } ?: libelle ?: "Sujet"
    val categoryLabel: String get() = categorie?.libelle?.ifBlank { null } ?: "Forum"
    val authorName: String get() = user?.displayName?.ifBlank { null } ?: "Membre PNSA"
    val replyCount: Int get() = commentairesCount ?: commentaires.size
    val likeCount: Int get() = likes?.toIntOrNull() ?: 0
    val publishedAt: String? get() = updatedAt ?: createdAt
}

@Serializable
data class Commentaire(
    val id: Long? = null,
    val code: String? = null,
    @SerialName("commentaire_id") val commentaireId: Long? = null,
    @SerialName("contenu_id") val contenuId: Long? = null,
    @SerialName("sujet_id") val sujetId: Long? = null,
    val commentaire: String? = null,
    val likes: String? = null,
    val unlikes: String? = null,
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val status: Boolean? = true,
    @SerialName("status_del")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val statusDel: Boolean? = true,
    @SerialName("user_id") val userId: Long? = null,
    val user: User? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class Discussion(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("receiver_user_id") val receiverUserId: Long? = null,
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val status: Boolean? = true,
    @SerialName("status_del")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val statusDel: Boolean? = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    val title: String get() = libelle?.ifBlank { null } ?: "Discussion privée"
}

@Serializable
data class Message(
    val id: Long? = null,
    val msg: String? = null,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("receiver_user_id") val receiverUserId: Long? = null,
    @SerialName("discussion_id") val discussionId: Long? = null,
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val status: Boolean? = true,
    @SerialName("status_del")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val statusDel: Boolean? = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class Messagerie(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)
