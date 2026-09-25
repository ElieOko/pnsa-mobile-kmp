package app.partners.pnsa.features.forum.domain.models

data class Categorie(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    val description: String? = null,
    val image: String? = null,
    val imagePath: String? = null,
    val status: Boolean? = true,
    val statusDel: Boolean? = true,
    val userId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class Sujet(
    val id: Long? = null,
    val code: String? = null,
    val title: String? = null,
    val description: String? = null,
    val image: String? = null,
    val imagePath: String? = null,
    val likes: String? = null,
    val unlikes: String? = null,
    val status: Boolean? = true,
    val statusDel: Boolean? = true,
    val userId: Long? = null,
    val categorieId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class Commentaire(
    val id: Long? = null,
    val code: String? = null,

    // Réponse à un autre commentaire
    val commentaireId: Long? = null,

    val contenuId: Long? = null,
    val sujetId: Long? = null,

    val commentaire: String? = null,

    val likes: String? = null,
    val unlikes: String? = null,

    val status: Boolean? = true,
    val statusDel: Boolean? = true,

    val userId: Long? = null,

    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class Messagerie(
    val id: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class Discussion(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,

    // Utilisateur qui initie la discussion
    val userId: Long? = null,

    // Destinataire
    val receiverUserId: Long? = null,

    val status: Boolean? = true,
    val statusDel: Boolean? = true,

    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class Message(
    val id: Long? = null,
    val msg: String? = null,

    // Expéditeur
    val userId: Long? = null,

    // Destinataire
    val receiverUserId: Long? = null,

    val discussionId: Long? = null,

    val status: Boolean? = true,
    val statusDel: Boolean? = true,

    val createdAt: String? = null,
    val updatedAt: String? = null
)