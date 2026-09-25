package app.partners.pnsa

import app.partners.pnsa.core.network.AppJson
import app.partners.pnsa.core.network.DataWrapper
import app.partners.pnsa.features.auth.domain.models.AuthTokenResponse
import app.partners.pnsa.features.quiz.domain.models.Quiz
import app.partners.pnsa.features.user.domain.models.ProfileBundle
import app.partners.pnsa.features.user.domain.models.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiJsonTest {

    @Test
    fun parsesAuthTokenAndUser() {
        val json = """
            {
              "token": "1|abc",
              "token_type": "Bearer",
              "user": {
                "id": 80,
                "nom": "Test",
                "prenom": "Mobile",
                "email": "a@b.c",
                "genre": "F",
                "status": 1,
                "status_del": 1,
                "province": "Kinshasa",
                "ville": "Kinshasa",
                "langue": "fr"
              }
            }
        """.trimIndent()
        val parsed = AppJson.decodeFromString(AuthTokenResponse.serializer(), json)
        assertEquals("1|abc", parsed.token)
        assertEquals("Mobile Test", parsed.user?.displayName)
        assertEquals(80L, parsed.user?.id)
    }

    @Test
    fun parsesQuizWithIntegerCorrectFlags() {
        val json = """
            {
              "id": 2,
              "libelle": "Quiz : Mythes et réalités",
              "nb_second": 60,
              "content_version": 1,
              "allow_offline": 1,
              "questionnaires": [
                {
                  "id": 8,
                  "question": "Une IST peut se transmettre sans rapport sexuel ?",
                  "point": 1,
                  "ordre": 1,
                  "reponses": [
                    {"id": 21, "reponse": "Oui", "is_correct": 1},
                    {"id": 22, "reponse": "Non", "is_correct": 0}
                  ]
                }
              ]
            }
        """.trimIndent()
        val quiz = AppJson.decodeFromString(Quiz.serializer(), json)
        assertEquals("Quiz : Mythes et réalités", quiz.title)
        assertEquals(1, quiz.questionCount)
        assertTrue(quiz.questionnaires.first().reponses.first().isCorrect)
        assertTrue(!quiz.questionnaires.first().reponses.last().isCorrect)
        assertEquals(true, quiz.allowOffline)
    }

    @Test
    fun parsesProfileBundle() {
        val json = """
            {
              "data": {
                "user": {"id": 1, "prenom": "Awa", "nom": "K", "email": "a@b.c", "status": true},
                "consents": [{"consent_type":"privacy","granted":true,"policy_version":"1.0"}],
                "notification_preferences": {
                  "push_enabled": true,
                  "forum_enabled": false,
                  "advice_enabled": 1,
                  "orientation_enabled": 0
                }
              }
            }
        """.trimIndent()
        val wrap = AppJson.decodeFromString(DataWrapper.serializer(ProfileBundle.serializer()), json)
        assertEquals("Awa K", wrap.data.user?.displayName)
        assertEquals(true, wrap.data.notificationPreferences?.pushEnabled)
        assertEquals(false, wrap.data.notificationPreferences?.forumEnabled)
        assertEquals(true, wrap.data.notificationPreferences?.adviceEnabled)
        assertEquals(false, wrap.data.notificationPreferences?.orientationEnabled)
    }

    @Test
    fun meEndpointParsesAsUser() {
        val json = """{"id":80,"nom":"Test","prenom":"Mobile","email":"a@b.c","status":1,"genre":"F"}"""
        val user = AppJson.decodeFromString(User.serializer(), json)
        assertEquals("MT", user.initials)
    }
}
