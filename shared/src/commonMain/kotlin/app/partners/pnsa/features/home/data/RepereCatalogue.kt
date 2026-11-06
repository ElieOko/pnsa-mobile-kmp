package app.partners.pnsa.features.home.data

import androidx.compose.ui.graphics.Color
import app.partners.pnsa.core.ui.theme.RepereCream
import app.partners.pnsa.core.ui.theme.RepereLilac
import app.partners.pnsa.core.ui.theme.RepereMint
import app.partners.pnsa.core.ui.theme.ReperePeach
import app.partners.pnsa.core.ui.theme.ReperePink
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import app.partners.pnsa.resources.Res
import app.partners.pnsa.resources.illu_contraception
import app.partners.pnsa.resources.illu_ecoute
import app.partners.pnsa.resources.illu_jeune
import app.partners.pnsa.resources.photo_consultation_couple
import app.partners.pnsa.resources.photo_consultation_femme
import app.partners.pnsa.resources.photo_consultation_homme
import app.partners.pnsa.resources.photo_couple_parc
import kotlin.math.absoluteValue
import org.jetbrains.compose.resources.DrawableResource

enum class RepereRole {
    Hero,
    Besoin,
    Carte,
}

data class RepereVisuel(
    val id: String,
    val titre: String,
    val detail: String,
    val image: DrawableResource,
    val filtre: String,
    val fond: Color,
    val role: RepereRole,
)

data class RepereFiltre(
    val label: String,
    val tokens: List<String>,
)

val RepereFiltres: List<RepereFiltre> = listOf(
    RepereFiltre("Tous", emptyList()),
    RepereFiltre("Conseil", listOf("IEC", "CCC", "conseil")),
    RepereFiltre("Contraception", listOf("Préservatif", "Contraception", "Pilule", "Implant", "Injectable", "DIU", "DMPA")),
    RepereFiltre("IST et VIH", listOf("IST", "VIH")),
    RepereFiltre("Violence", listOf("VBG", "violence")),
)

fun matchesFiltre(services: String?, label: String): Boolean {
    val filtre = RepereFiltres.firstOrNull { it.label == label } ?: return label == "Tous"
    if (filtre.tokens.isEmpty()) return true
    val blob = services.orEmpty()
    return filtre.tokens.any { token -> blob.contains(token, ignoreCase = true) }
}

object RepereCatalogue {
    val hero: RepereVisuel = RepereVisuel(
        id = "couple-parc",
        titre = "Des services près de toi",
        detail = "Un accompagnement pour avancer à deux, près de chez toi.",
        image = Res.drawable.photo_couple_parc,
        filtre = "Tous",
        fond = RepereMint,
        role = RepereRole.Hero,
    )

    val besoins: List<RepereVisuel> = listOf(
        RepereVisuel(
            id = "contraception",
            titre = "Contraception",
            detail = "Préservatif et méthodes adaptées, expliquées simplement.",
            image = Res.drawable.illu_contraception,
            filtre = "Contraception",
            fond = ReperePeach,
            role = RepereRole.Besoin,
        ),
        RepereVisuel(
            id = "ist-vih",
            titre = "IST et VIH",
            detail = "Dépistage et prise en charge, dans un cadre confidentiel.",
            image = Res.drawable.illu_jeune,
            filtre = "IST et VIH",
            fond = RepereCream,
            role = RepereRole.Besoin,
        ),
        RepereVisuel(
            id = "ecoute",
            titre = "Écoute et conseil",
            detail = "Parler à un professionnel, sans jugement.",
            image = Res.drawable.illu_ecoute,
            filtre = "Conseil",
            fond = RepereLilac,
            role = RepereRole.Besoin,
        ),
        RepereVisuel(
            id = "violence",
            titre = "Aide après une violence",
            detail = "Un accueil bienveillant, avec une personne formée.",
            image = Res.drawable.photo_consultation_femme,
            filtre = "Violence",
            fond = ReperePink,
            role = RepereRole.Besoin,
        ),
    )

    val cartes: List<RepereVisuel> = listOf(
        RepereVisuel(
            id = "consultation-lui",
            titre = "Une consultation pour lui",
            detail = "Poser ses questions à un médecin, y compris sur le corps.",
            image = Res.drawable.photo_consultation_homme,
            filtre = "IST et VIH",
            fond = RepereMint,
            role = RepereRole.Carte,
        ),
        RepereVisuel(
            id = "venir-a-deux",
            titre = "Venir à deux",
            detail = "Un rendez-vous possible en couple, en toute confidentialité.",
            image = Res.drawable.photo_consultation_couple,
            filtre = "Contraception",
            fond = RepereLilac,
            role = RepereRole.Carte,
        ),
    )

    val tous: List<RepereVisuel> = listOf(hero) + besoins + cartes

    fun byId(id: String): RepereVisuel? = tous.firstOrNull { it.id == id }
}

fun coverForStructure(id: Long?): DrawableResource {
    val images = RepereCatalogue.tous.map { it.image }
    val index = (id ?: 0L).absoluteValue % images.size
    return images[index.toInt()]
}

fun HealthStructure.pastilles(): List<String> {
    val services = this.services.orEmpty()
    return buildList {
        if (matchesFiltre(services, "Contraception")) add("Contraception")
        if (matchesFiltre(services, "IST et VIH")) add("IST et VIH")
        if (matchesFiltre(services, "Conseil")) add("Écoute et conseil")
        if (matchesFiltre(services, "Violence")) add("Aide après une violence")
    }
}

fun HealthStructure.espaceJeunes(): Boolean {
    val blob = "$displayName ${services.orEmpty()} ${structureType.orEmpty()}"
    return blob.contains("jeune", ignoreCase = true) ||
        matchesFiltre(services, "Conseil") ||
        matchesFiltre(services, "Violence")
}
