package app.partners.pnsa.features.quiz.domain.models

data class TypeQuestionnaire(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    val status: Boolean? = true,
    val statusDel: Boolean? = true,
    val userId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class Quiz(
    val id: Long? = null,
    val code: String? = null,
    val libelle: String? = null,
    val image: String? = null,
    val imagePath: String? = null,
    val description: String? = null,
    val nbSecond: Int? = null,
    val status: Boolean? = true,
    val statusDel: Boolean? = true,
    val userId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class Questionnaire(
    val id: Long? = null,
    val code: String? = null,
    val question: String? = null,
    val point: Int? = null,
    val ordre: Int? = null,
    val image: String? = null,
    val imagePath: String? = null,
    val status: Boolean? = true,
    val statusDel: Boolean? = true,
    val typeQuestionnaireId: Long? = null,
    val userId: Long? = null,
    val quizId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class Reponse(
    val id: Long? = null,
    val code: String? = null,
    val reponse: String? = null,
    val isCorrect: Boolean? = false,
    val status: Boolean? = true,
    val statusDel: Boolean? = true,
    val userId: Long? = null,
    val questionnaireId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)