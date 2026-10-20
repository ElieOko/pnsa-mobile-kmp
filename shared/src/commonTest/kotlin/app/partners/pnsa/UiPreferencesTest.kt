package app.partners.pnsa

import app.partners.pnsa.core.data.KeyValueStore
import app.partners.pnsa.core.data.UiPreferences
import app.partners.pnsa.core.ui.map.MapViewStyle
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.navigation.MainTab
import app.partners.pnsa.core.ui.theme.bagdeColor
import app.partners.pnsa.core.ui.theme.wsColor
import app.partners.pnsa.features.forum.domain.models.Categorie
import app.partners.pnsa.features.forum.domain.models.Sujet
import app.partners.pnsa.features.user.domain.models.User
import androidx.compose.ui.graphics.Color
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
        prefs.mapStyle = MapViewStyle.Satellite.name
        prefs.mapCity = "Gombe"
        prefs.mapQuery = "SSR"
        prefs.lastQuizId = 12
        prefs.lastQuizQuestionIndex = 3
        prefs.contentCategory = "Santé"
        prefs.contentQuery = "contraception"
        assertEquals("Puberté", prefs.forumCategory)
        assertEquals(false, prefs.mapShowMap)
        assertEquals("Satellite", prefs.mapStyle)
        assertEquals("Gombe", prefs.mapCity)
        assertEquals("SSR", prefs.mapQuery)
        assertEquals(12L, prefs.lastQuizId)
        assertEquals(3, prefs.lastQuizQuestionIndex)
        assertEquals("Santé", prefs.contentCategory)
        assertEquals("contraception", prefs.contentQuery)
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
            commentairesCount = 7,
        )
        assertEquals("Puberté", sujet.categoryLabel)
        assertEquals("Amina K.", sujet.authorName)
        assertEquals(7, sujet.replyCount)
        assertEquals(4, sujet.likeCount)
    }

    @Test
    fun whatsappThemeColorsAndTabsAreConfigured() {
        assertEquals(Color.Green.copy(0.3f), wsColor)
        assertEquals(Color(0xFF1AB221), bagdeColor)
        val store = MemoryStore()
        val prefs = UiPreferences(store)
        val nav = AppNavigator(AppDestination.Structures, onStateChanged = { })
        assertEquals(MainTab.Structures, nav.tab)
        nav.push(AppDestination.Messages)
        assertEquals(MainTab.Messages, nav.tab)
        nav.push(AppDestination.Profile)
        assertEquals(MainTab.Profile, nav.tab)
        nav.push(AppDestination.Advice)
        assertEquals(MainTab.Messages, nav.tab)
        nav.saveTo(prefs)

        val restored = AppNavigator(AppDestination.Structures)
        restored.restoreFrom(prefs)
        assertEquals(MainTab.Messages, restored.tab)
        assertEquals(AppDestination.Advice, restored.current)
    }

    @Test
    fun chatMessagingDoesNotAutoReplyAndPersistsAcrossRestarts() {
        val store = MemoryStore()
        val repo = app.partners.pnsa.features.structure.data.StructureMessagingRepository(store)
        val center = app.partners.pnsa.features.structure.data.KinshasaCenters.all.first()
        val centerId = center.id!!
        val initialCount = repo.messagesFor(center).size
        repo.sendToStructure(center, "Bonjour, avez-vous un médecin ?")
        repo.saveDraft(centerId, "Brouillon test")
        val afterSend = repo.messagesFor(center)
        assertEquals(initialCount + 1, afterSend.size)
        assertEquals(false, afterSend.last().fromStructure)
        assertEquals("Bonjour, avez-vous un médecin ?", afterSend.last().text)

        val reloadedRepo = app.partners.pnsa.features.structure.data.StructureMessagingRepository(store)
        assertEquals(afterSend.size, reloadedRepo.messagesFor(center).size)
        assertEquals("Bonjour, avez-vous un médecin ?", reloadedRepo.messagesFor(center).last().text)
        assertEquals("Brouillon test", reloadedRepo.draftFor(centerId))
    }
}
