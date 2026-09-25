package app.partners.pnsa.features.quiz.data

import app.partners.pnsa.core.network.ApiClient
import app.partners.pnsa.core.network.DataWrapper
import app.partners.pnsa.core.network.PageDto
import app.partners.pnsa.core.util.QuizScoring
import app.partners.pnsa.core.util.randomUuid
import app.partners.pnsa.features.quiz.domain.models.QuizAttempt
import app.partners.pnsa.features.quiz.domain.models.QuizAttemptAnswersRequest
import app.partners.pnsa.features.quiz.domain.models.QuizAttemptStartRequest
import app.partners.pnsa.features.quiz.domain.models.QuizAttemptSubmitRequest
import app.partners.pnsa.features.quiz.domain.models.Questionnaire

class QuizAttemptRepository(
    private val api: ApiClient,
) {
    suspend fun history(): PageDto<QuizAttempt> =
        api.get("mobile/v1/quiz-attempts", PageDto.serializer(QuizAttempt.serializer()))

    suspend fun start(quizId: Long, clientAttemptId: String = randomUuid()): QuizAttempt {
        return api.post(
            path = "mobile/v1/quiz-attempts/start",
            serializer = DataWrapper.serializer(QuizAttempt.serializer()),
            body = QuizAttemptStartRequest(quizId = quizId, clientAttemptId = clientAttemptId),
        ).data
    }

    suspend fun saveProgress(clientAttemptId: String, answers: Map<Long, Long>): QuizAttempt {
        return api.patch(
            path = "mobile/v1/quiz-attempts/$clientAttemptId/progress",
            serializer = DataWrapper.serializer(QuizAttempt.serializer()),
            body = QuizAttemptAnswersRequest(answers = QuizScoring.toPayload(answers)),
        ).data
    }

    suspend fun submit(
        clientAttemptId: String,
        questions: List<Questionnaire>,
        answers: Map<Long, Long>,
    ): QuizAttempt {
        val score = QuizScoring.localScore(questions, answers)
        return api.post(
            path = "mobile/v1/quiz-attempts/$clientAttemptId/submit",
            serializer = DataWrapper.serializer(QuizAttempt.serializer()),
            body = QuizAttemptSubmitRequest(
                answers = QuizScoring.toPayload(answers),
                scoreLocal = score,
            ),
        ).data
    }

    suspend fun abandon(clientAttemptId: String): QuizAttempt {
        return api.post(
            path = "mobile/v1/quiz-attempts/$clientAttemptId/abandon",
            serializer = DataWrapper.serializer(QuizAttempt.serializer()),
        ).data
    }
}
