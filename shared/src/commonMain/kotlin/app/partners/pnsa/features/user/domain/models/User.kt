package app.partners.pnsa.features.user.domain.models



data class User(
    val id: Long? = null,
    val nom: String? = null,
    val prenom: String? = null,
    val phone: String? = null,
    val matricule: String? = null,
    val dateNaissance: String? = null,
    val lieuNaissance: String? = null,
    val lieuResidence: String? = null,
    val email: String? = null,
    val password: String? = null,
    val typeUserId: Long? = null,
    val passwordClear: String? = null,
    val type: String? = null,
    val status: Boolean? = true,
    val statusDel: Boolean? = true,
    val rememberToken: String? = null,
    val emailVerifiedAt: String? = null,
    val genre: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class TypeUser(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    val status: Boolean? = true,
    val statusDel: Boolean? = true,
    val userId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)



