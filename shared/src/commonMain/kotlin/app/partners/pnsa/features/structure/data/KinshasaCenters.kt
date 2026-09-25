package app.partners.pnsa.features.structure.data

import app.partners.pnsa.features.structure.domain.models.HealthStructure

object KinshasaCenters {
    val all: List<HealthStructure> = listOf(
        center(-101, "Siège PNSA", "Gombe", "Avenue de la Justice, Gombe", -4.3058, 15.3136, "Programme national", "Orientation SSR, information, coordination"),
        center(-102, "Hôpital Général de Référence de Kinshasa", "Gombe", "Boulevard Triomphal, Lingwala / Gombe", -4.3276, 15.3136, "Hôpital de référence", "Urgences, SSR, consultations, laboratoire"),
        center(-103, "Hôpital Ngaliema", "Ngaliema", "Avenue des Cliniques, Ngaliema", -4.3248, 15.2708, "Hôpital", "Maternité, consultations, santé de la mère et de l’enfant"),
        center(-104, "Centre de Santé de Référence de Kintambo", "Kintambo", "Avenue Kasa-Vubu, Kintambo", -4.3272, 15.2754, "CSR", "Planification familiale, IST, consultations SSR"),
        center(-105, "Hôpital Saint Joseph", "Limete", "Boulevard Lumumba, Limete", -4.3816, 15.3452, "Hôpital confessionnel", "Consultations, maternité, laboratoire"),
        center(-106, "Cliniques Universitaires de Kinshasa", "Lemba", "Mont Amba, Lemba", -4.4218, 15.3095, "CHU", "Référence universitaire, gynécologie, pédiatrie"),
        center(-107, "Centre Hospitalier Monkole", "Mont Ngafula", "Mont Ngafula", -4.4582, 15.2684, "Hôpital", "Mère-enfant, consultations, hospitalisation"),
        center(-108, "Hôpital Roi Baudouin", "Kimbanseke", "Kingasani, Kimbanseke", -4.4012, 15.4088, "Hôpital", "Urgences, SSR, consultations générales"),
        center(-109, "Centre de Santé de Masina", "Masina", "Boulevard Lumumba, Masina", -4.3848, 15.3648, "Centre de santé", "CPN, vaccination, planification familiale"),
        center(-110, "Hôpital de Kalembe Lembe", "Kinshasa", "Kalembe Lembe, Kinshasa", -4.3382, 15.3224, "Hôpital pédiatrique", "Santé de l’adolescent, pédiatrie"),
        center(-111, "Centre Hospitalier de Kingasani", "Kimbanseke", "Kingasani", -4.3924, 15.3982, "Hôpital", "Consultations, maternité, IST"),
        center(-112, "Dispensaire de la Gombe", "Gombe", "Avenue Tombalbaye, Gombe", -4.3054, 15.3088, "Dispensaire", "Dépistage volontaire, conseil, orientation"),
        center(-113, "Centre Mère et Enfant de Binza", "Ngaliema", "Binza/UPN, Ngaliema", -4.3654, 15.2456, "CME", "Santé maternelle, adolescent, nutrition"),
        center(-114, "Centre de Santé de Ndjili", "Ndjili", "Quartier 7, Ndjili", -4.4086, 15.3784, "Centre de santé", "SSR, CPN, planning familial"),
        center(-115, "Hôpital de la Rive / Bondeko", "Barumbu", "Avenue de la Rive, Barumbu", -4.3182, 15.3286, "Hôpital", "Consultations, urgences, orientation SSR"),
    )

    fun byId(id: Long): HealthStructure? = all.firstOrNull { it.id == id }

    fun mergeWith(remote: List<HealthStructure>): List<HealthStructure> {
        if (remote.isEmpty()) return all
        val names = remote.map { it.displayName.lowercase() }.toSet()
        val extras = all.filter { it.displayName.lowercase() !in names }
        return remote + extras
    }

    private fun center(
        id: Long,
        name: String,
        city: String,
        address: String,
        lat: Double,
        lon: Double,
        type: String,
        services: String,
    ) = HealthStructure(
        id = id,
        name = name,
        nom = name,
        province = "Kinshasa",
        city = city,
        ville = city,
        address = address,
        adresse = address,
        latitude = lat,
        longitude = lon,
        phone = "+243 862 01 1506",
        email = "secretariat@pnsa.cd",
        services = services,
        structureType = type,
        openingHours = "Lun–Ven 08:00–16:00",
        status = "active",
    )
}
