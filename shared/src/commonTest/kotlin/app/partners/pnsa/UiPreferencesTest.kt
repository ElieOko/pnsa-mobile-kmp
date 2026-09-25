package app.partners.pnsa

import app.partners.pnsa.core.data.KeyValueStore
import app.partners.pnsa.core.data.UiPreferences
import app.partners.pnsa.features.forum.domain.models.Categorie
import app.partners.pnsa.features.forum.domain.models.Sujet
import app.partners.pnsa.features.user.domain.models.User
import kotlin.test.Test
import kotlin.test.assertEquals

class MemoryStore : KeyValueStore {
    private val values = mutableMapOf<String, String>()
    override fun get(key: String): String? = values[key]
    override fun put(key: String, value: String?) {
        if (value == null) values.remove(key) else values[key] = value
    }
}

class UiPreferencesTest {
    @Test
    fun persistsNonSensitiveFilters() {
        val prefs = UiPreferences(MemoryStore())
        prefs.forumCategory = "Puberté"
        prefs.mapShowMap = false
        prefs.mapCity = "Gombe"
        prefs.mapQuery = "SSR"
        prefs.lastQuizId = 12
        assertEquals("Puberté", prefs.forumCategory)
        assertEquals(false, prefs.mapShowMap)
        assertEquals("Gombe", prefs.mapCity)
        assertEquals("SSR", prefs.mapQuery)
        assertEquals(12L, prefs.lastQuizId)
    }

    @Test
    fun forumPostExposesHierarchy() {
        val sujet = Sujet(
            title = "Âge de la puberté",
            description = "Question",
            likes = "4",
            categorie = Categorie(libelle = "Puberté"),
            user = User(prenom = "Amina", nom = "K."),
            commentaires = emptyList(),
        )
        assertEquals("Puberté", sujet.categoryLabel)
        assertEquals("Amina K.", sujet.authorName)
        assertEquals(0, sujet.replyCount)
        assertEquals(4, sujet.likeCount)
    }
}
