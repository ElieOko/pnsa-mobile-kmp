package app.partners.pnsa.core.data

import app.partners.pnsa.features.content.domain.models.Contenu
import app.partners.pnsa.features.content.domain.models.HomeFeed
import app.partners.pnsa.features.forum.domain.models.Sujet
import app.partners.pnsa.features.quiz.domain.models.Quiz
import app.partners.pnsa.features.quiz.domain.models.QuizAttempt
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import app.partners.pnsa.features.user.domain.models.ProfileBundle

class ScreenStore {
    var home: HomeFeed? = null
    var homeLoaded: Boolean = false
    var contenus: List<Contenu> = emptyList()
    var contenusLoaded: Boolean = false
    var quizzes: List<Quiz> = emptyList()
    var quizAttempts: List<QuizAttempt> = emptyList()
    var quizzesLoaded: Boolean = false
    var structures: List<HealthStructure> = emptyList()
    var structuresLoaded: Boolean = false
    var sujets: List<Sujet> = emptyList()
    var sujetsLoaded: Boolean = false
    var profile: ProfileBundle? = null
    var profileLoaded: Boolean = false

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
