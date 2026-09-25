package app.partners.pnsa.core.data

import app.partners.pnsa.features.content.domain.models.Actualite
import app.partners.pnsa.features.content.domain.models.Contenu
import app.partners.pnsa.features.content.domain.models.Faq
import app.partners.pnsa.features.forum.domain.models.Categorie
import app.partners.pnsa.features.quiz.domain.models.Quiz
import app.partners.pnsa.features.structure.domain.models.HealthStructure

class CatalogCache {
    var contenus: List<Contenu> = emptyList()
        private set
    var quizzes: List<Quiz> = emptyList()
        private set
    var actualites: List<Actualite> = emptyList()
        private set
    var faqs: List<Faq> = emptyList()
        private set
    var structures: List<HealthStructure> = emptyList()
        private set
    var categories: List<Categorie> = emptyList()
        private set
    var lastCatalogSync: String? = null
        private set
    var lastStructureSync: String? = null
        private set

    fun putCatalog(
        contenus: List<Contenu>? = null,
        quizzes: List<Quiz>? = null,
        actualites: List<Actualite>? = null,
        faqs: List<Faq>? = null,
        categories: List<Categorie>? = null,
        syncedAt: String? = null,
    ) {
        if (contenus != null) this.contenus = mergeById(this.contenus, contenus) { it.id }
        if (quizzes != null) this.quizzes = mergeById(this.quizzes, quizzes) { it.id }
        if (actualites != null) this.actualites = mergeById(this.actualites, actualites) { it.id }
        if (faqs != null) this.faqs = mergeById(this.faqs, faqs) { it.id }
        if (categories != null) this.categories = mergeById(this.categories, categories) { it.id }
        if (syncedAt != null) lastCatalogSync = syncedAt
    }

    fun putStructures(items: List<HealthStructure>, syncedAt: String?) {
        structures = mergeById(structures, items) { it.id }
        lastStructureSync = syncedAt ?: lastStructureSync
    }

    fun clear() {
        contenus = emptyList()
        quizzes = emptyList()
        actualites = emptyList()
        faqs = emptyList()
        structures = emptyList()
        categories = emptyList()
        lastCatalogSync = null
        lastStructureSync = null
    }

    private fun <T> mergeById(current: List<T>, incoming: List<T>, id: (T) -> Long?): List<T> {
        if (incoming.isEmpty()) return current.ifEmpty { incoming }
        val map = LinkedHashMap<Long, T>()
        current.forEach { item -> id(item)?.let { map[it] = item } }
        incoming.forEach { item -> id(item)?.let { map[it] = item } }
        return map.values.toList()
    }
}
