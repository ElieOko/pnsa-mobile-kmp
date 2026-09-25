package app.partners.pnsa.features.quiz.domain.models

import app.partners.pnsa.core.network.FlexibleBoolSerializer
import app.partners.pnsa.core.network.NullableFlexibleBoolSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TypeQuestionnaire(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val status: Boolean? = true,
    @SerialName("status_del")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val statusDel: Boolean? = true,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class Quiz(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    val image: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    val description: String? = null,
    @SerialName("nb_second") val nbSecond: Int? = null,
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val status: Boolean? = true,
    @SerialName("status_del")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val statusDel: Boolean? = true,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("content_version") val contentVersion: Int? = null,
    val langue: String? = null,
    val theme: String? = null,
    @SerialName("allow_offline")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val allowOffline: Boolean? = null,
    @SerialName("editorial_status") val editorialStatus: String? = null,
    val questionnaires: List<Questionnaire> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    val title: String get() = libelle?.ifBlank { null } ?: "Quiz"
    val questionCount: Int get() = questionnaires.size
}

@Serializable
data class Questionnaire(
    val id: Long? = null,
    val code: String? = null,
    val question: String? = null,
    val libelle: String? = null,
    val point: Int? = null,
    val ordre: Int? = null,
    val image: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val status: Boolean? = true,
    @SerialName("status_del")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val statusDel: Boolean? = true,
    @SerialName("type_questionnaire_id") val typeQuestionnaireId: Long? = null,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("quiz_id") val quizId: Long? = null,
    val reponses: List<Reponse> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    val prompt: String get() = question?.ifBlank { null } ?: libelle ?: "Question"
}

@Serializable
data class Reponse(
    val id: Long? = null,
    val code: String? = null,
    val reponse: String? = null,
    @SerialName("is_correct")
    @Serializable(with = FlexibleBoolSerializer::class)
    val isCorrect: Boolean = false,
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val status: Boolean? = true,
    @SerialName("status_del")
    @Serializable(with = NullableFlexibleBoolSerializer::class)
    val statusDel: Boolean? = true,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("questionnaire_id") val questionnaireId: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class QuizAttempt(
    val id: Long? = null,
    @SerialName("client_attempt_id") val clientAttemptId: String? = null,
    @SerialName("quiz_id") val quizId: Long? = null,
    @SerialName("quiz_version") val quizVersion: Int? = null,
    val status: String? = null,
    @SerialName("score_local") val scoreLocal: Int? = null,
    @SerialName("score_final") val scoreFinal: Int? = null,
    @SerialName("total_questions") val totalQuestions: Int? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("last_activity_at") val lastActivityAt: String? = null,
    val answers: List<QuizAnswerSnapshot> = emptyList(),
) {
    val displayScore: Int? get() = scoreFinal ?: scoreLocal
    val isOpen: Boolean get() = status == "in_progress"
}

@Serializable
data class QuizAnswerSnapshot(
    @SerialName("questionnaire_id") val questionnaireId: Long? = null,
    @SerialName("reponse_id") val reponseId: Long? = null,
)

@Serializable
data class QuizAttemptStartRequest(
    @SerialName("quiz_id") val quizId: Long,
    @SerialName("client_attempt_id") val clientAttemptId: String,
)

@Serializable
data class QuizAnswerPayload(
    @SerialName("questionnaire_id") val questionnaireId: Long,
    @SerialName("reponse_id") val reponseId: Long? = null,
)

@Serializable
data class QuizAttemptAnswersRequest(
    val answers: List<QuizAnswerPayload>,
)

@Serializable
data class QuizAttemptSubmitRequest(
    val answers: List<QuizAnswerPayload>,
    @SerialName("score_local") val scoreLocal: Int? = null,
)
