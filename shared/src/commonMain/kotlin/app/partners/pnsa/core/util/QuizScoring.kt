package app.partners.pnsa.core.util

import app.partners.pnsa.features.quiz.domain.models.Questionnaire
import app.partners.pnsa.features.quiz.domain.models.QuizAnswerPayload

object QuizScoring {
    fun localScore(
        questions: List<Questionnaire>,
        answers: Map<Long, Long>,
    ): Int {
        return questions.sumOf { question ->
            val selected = question.id?.let { answers[it] } ?: return@sumOf 0
            val correct = question.reponses.firstOrNull { it.id == selected }?.isCorrect == true
            if (correct) (question.point ?: 1) else 0
        }
    }

    fun maxScore(questions: List<Questionnaire>): Int =
        questions.sumOf { it.point ?: 1 }

    fun toPayload(answers: Map<Long, Long>): List<QuizAnswerPayload> =
        answers.map { (questionId, responseId) ->
            QuizAnswerPayload(questionnaireId = questionId, reponseId = responseId)
        }
}
