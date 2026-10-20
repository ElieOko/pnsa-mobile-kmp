package app.partners.pnsa.core.data

import app.partners.pnsa.core.network.AppJson
import app.partners.pnsa.features.content.domain.models.Contenu
import app.partners.pnsa.features.content.domain.models.HomeFeed
import app.partners.pnsa.features.forum.domain.models.Sujet
import app.partners.pnsa.features.quiz.domain.models.Quiz
import app.partners.pnsa.features.quiz.domain.models.QuizAttempt
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import app.partners.pnsa.features.user.domain.models.ProfileBundle
import kotlinx.serialization.encodeToString

class ScreenStore(
    private val store: KeyValueStore? = null,
) {
    var home: HomeFeed? = store?.get("screen.home")?.let { runCatching { AppJson.decodeFromString<HomeFeed>(it) }.getOrNull() }
        set(value) {
            field = value
            if (value == null) store?.put("screen.home", null)
            else runCatching { AppJson.encodeToString(value) }.onSuccess { store?.put("screen.home", it) }
        }
    var homeLoaded: Boolean = home != null

    var contenus: List<Contenu> = store?.get("screen.contenus")?.let { runCatching { AppJson.decodeFromString<List<Contenu>>(it) }.getOrNull() } ?: emptyList()
        set(value) {
            field = value
            if (value.isEmpty()) store?.put("screen.contenus", null)
            else runCatching { AppJson.encodeToString(value) }.onSuccess { store?.put("screen.contenus", it) }
        }
    var contenusLoaded: Boolean = contenus.isNotEmpty()

    var quizzes: List<Quiz> = store?.get("screen.quizzes")?.let { runCatching { AppJson.decodeFromString<List<Quiz>>(it) }.getOrNull() } ?: emptyList()
        set(value) {
            field = value
            if (value.isEmpty()) store?.put("screen.quizzes", null)
            else runCatching { AppJson.encodeToString(value) }.onSuccess { store?.put("screen.quizzes", it) }
        }
    var quizAttempts: List<QuizAttempt> = store?.get("screen.quiz_attempts")?.let { runCatching { AppJson.decodeFromString<List<QuizAttempt>>(it) }.getOrNull() } ?: emptyList()
        set(value) {
            field = value
            if (value.isEmpty()) store?.put("screen.quiz_attempts", null)
            else runCatching { AppJson.encodeToString(value) }.onSuccess { store?.put("screen.quiz_attempts", it) }
        }
    var quizzesLoaded: Boolean = quizzes.isNotEmpty()

    var structures: List<HealthStructure> = store?.get("screen.structures")?.let { runCatching { AppJson.decodeFromString<List<HealthStructure>>(it) }.getOrNull() } ?: emptyList()
        set(value) {
            field = value
            if (value.isEmpty()) store?.put("screen.structures", null)
            else runCatching { AppJson.encodeToString(value) }.onSuccess { store?.put("screen.structures", it) }
        }
    var structuresLoaded: Boolean = structures.isNotEmpty()

    var sujets: List<Sujet> = store?.get("screen.sujets")?.let { runCatching { AppJson.decodeFromString<List<Sujet>>(it) }.getOrNull() } ?: emptyList()
        set(value) {
            field = value
            if (value.isEmpty()) store?.put("screen.sujets", null)
            else runCatching { AppJson.encodeToString(value) }.onSuccess { store?.put("screen.sujets", it) }
        }
    var sujetsLoaded: Boolean = sujets.isNotEmpty()

    var profile: ProfileBundle? = store?.get("screen.profile")?.let { runCatching { AppJson.decodeFromString<ProfileBundle>(it) }.getOrNull() }
        set(value) {
            field = value
            if (value == null) store?.put("screen.profile", null)
            else runCatching { AppJson.encodeToString(value) }.onSuccess { store?.put("screen.profile", it) }
        }
    var profileLoaded: Boolean = profile != null

    fun clear() {
        home = null
        homeLoaded = false
        contenus = emptyList()
        contenusLoaded = false
        quizzes = emptyList()
        quizAttempts = emptyList()
        quizzesLoaded = false
        structures = emptyList()
        structuresLoaded = false
        sujets = emptyList()
        sujetsLoaded = false
        profile = null
        profileLoaded = false
    }
}
