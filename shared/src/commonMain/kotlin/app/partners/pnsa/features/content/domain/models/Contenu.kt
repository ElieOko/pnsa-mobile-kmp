package app.partners.pnsa.features.content.domain.models

data class Contenu(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    val description: String? = null,
    val thumbnail: String? = null,
    val thumbnailPath: String? = null,
    val file: String? = null,
    val filePath: String? = null,
    val extension: String? = null,
    val likes: String? = null,
    val unlikes: String? = null,
    val vieweds: String? = null,
    val status: Boolean? = true,
    val statusDel: Boolean? = true,
    val userId: Long? = null,
    val typeContenuId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)


data class TypeContenu(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    val image: String? = null,
    val imagePath: String? = null,
    val status: Boolean? = true,
    val statusDel: Boolean? = true,
    val userId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class Actualite(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    val description: String? = null,

    val file: String? = null,
    val filePath: String? = null,
    val extension: String? = null,

    val thumbnail: String? = null,
    val thumbnailPath: String? = null,

    val likes: String? = null,
    val unlikes: String? = null,
    val vieweds: String? = null,

    val status: Boolean? = true,
    val statusDel: Boolean? = true,

    val userId: Long? = null,
    val typeContenuId: Long? = null,

    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class Faq(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    val description: String? = null,
    val status: Boolean? = true,
    val statusDel: Boolean? = true,
    val userId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)