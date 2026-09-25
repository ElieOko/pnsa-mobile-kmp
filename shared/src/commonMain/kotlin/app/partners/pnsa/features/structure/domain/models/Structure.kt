package app.partners.pnsa.features.structure.domain.models

data class Activite(
    val id: Long? = null,
    val nom: String? = null,
    val dateDebut: String? = null,
    val dateFin: String? = null,
    val mois: Int? = null,
    val trimestre: Int? = null,
    val lieu: String? = null,
    val structureResponsable: String? = null,
    val autreService: String? = null,
    val ligneBudgetaire: Int? = null,
    val montant: Int? = null,
    val bePtf: String? = null,
    val nomRaporteur: String? = null,
    val status: Boolean? = true,
    val statusDel: Boolean? = true,
    val userId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)