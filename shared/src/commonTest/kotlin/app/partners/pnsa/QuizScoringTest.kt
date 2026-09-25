package app.partners.pnsa

import app.partners.pnsa.core.util.QuizScoring
import app.partners.pnsa.core.util.randomUuid
import app.partners.pnsa.features.quiz.domain.models.Questionnaire
import app.partners.pnsa.features.quiz.domain.models.Reponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuizScoringTest {

    private val questions = listOf(
        Questionnaire(
            id = 8,
            question = "Une IST peut se transmettre sans rapport sexuel ?",
            point = 1,
            reponses = listOf(
                Reponse(id = 21, reponse = "Oui", isCorrect = true),
                Reponse(id = 22, reponse = "Non", isCorrect = false),
            ),
        ),
        Questionnaire(
            id = 9,
            question = "Le consentement est obligatoire",
            point = 2,
            reponses = listOf(
                Reponse(id = 31, reponse = "Oui", isCorrect = true),
                Reponse(id = 32, reponse = "Non", isCorrect = false),
            ),
        ),
    )

    @Test
    fun scoresCorrectAnswersWithQuestionPoints() {
        val score = QuizScoring.localScore(questions, mapOf(8L to 21L, 9L to 31L))
        assertEquals(3, score)
        assertEquals(3, QuizScoring.maxScore(questions))
    }

    @Test
    fun ignoresWrongAndMissingAnswers() {
        val score = QuizScoring.localScore(questions, mapOf(8L to 22L))
        assertEquals(0, score)
    }

    @Test
    fun buildsPayloadForServerSubmit() {
        val payload = QuizScoring.toPayload(mapOf(8L to 21L))
        assertEquals(1, payload.size)
        assertEquals(8L, payload.first().questionnaireId)
        assertEquals(21L, payload.first().reponseId)
    }

    @Test
    fun uuidLooksStableAndUnique() {
        val first = randomUuid()
        val second = randomUuid()
        assertTrue(first.contains("-"))
        assertEquals(36, first.length)
        assertTrue(first != second)
    }
}
