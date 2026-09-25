package app.partners.pnsa.features.quiz.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.partners.pnsa.resources.Res
import app.partners.pnsa.resources.quiz_question
import org.jetbrains.compose.resources.painterResource
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.network.ApiException
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.ErrorState
import app.partners.pnsa.core.ui.components.LoadingState
import app.partners.pnsa.core.ui.components.MetaRow
import app.partners.pnsa.core.ui.components.PnsaCard
import app.partners.pnsa.core.ui.components.PnsaTopBar
import app.partners.pnsa.core.ui.components.PrimaryAction
import app.partners.pnsa.core.ui.components.QuietAction
import app.partners.pnsa.core.ui.components.SecondaryAction
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.util.QuizScoring
import app.partners.pnsa.core.util.excerpt
import app.partners.pnsa.features.quiz.domain.models.Quiz
import app.partners.pnsa.features.quiz.domain.models.QuizAttempt
import kotlinx.coroutines.launch

@Composable
fun QuizListScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    var quizzes by remember { mutableStateOf<List<Quiz>>(emptyList()) }
    var attempts by remember { mutableStateOf<List<QuizAttempt>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            runCatching {
                val page = graph.catalog.quizzes(perPage = 50)
                val history = runCatching { graph.quizAttempts.history() }.getOrNull()
                page.data to (history?.data ?: emptyList())
            }.onSuccess { (list, history) ->
                quizzes = list
                attempts = history
                graph.screens.quizzes = list
                graph.screens.quizAttempts = history
                graph.screens.quizzesLoaded = true
            }.onFailure { throwable ->
                quizzes = graph.catalog.cachedQuizzes()
                if (quizzes.isNotEmpty()) {
                    graph.screens.quizzes = quizzes
                    graph.screens.quizzesLoaded = true
                }
                if (quizzes.isEmpty()) error = (throwable as? ApiException)?.userMessage() ?: throwable.message
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        if (graph.screens.quizzesLoaded) {
            quizzes = graph.screens.quizzes
            attempts = graph.screens.quizAttempts
            loading = false
        } else {
            load()
        }
    }

    Scaffold(topBar = { PnsaTopBar("Quiz") }) { padding ->
        when {
            loading -> LoadingState("Chargement des quiz…")
            error != null -> ErrorState(error.orEmpty(), onRetry = { load() })
            quizzes.isEmpty() -> EmptyState("Aucun quiz publié", "Les quiz validés par le PNSA apparaîtront ici.")
            else -> LazyColumn(
                Modifier.padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(22.dp)),
                    ) {
                        Image(
                            painterResource(Res.drawable.quiz_question),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.horizontalGradient(listOf(Color(0xCC0069E1), Color(0x99D01D2A))),
                            ),
                        )
                        Text(
                            "Tester tes connaissances SSR",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    StatusBanner("Tu peux interrompre un quiz et le reprendre. Le score définitif est confirmé par le serveur.")
                }
                items(quizzes, key = { it.id ?: it.title }) { quiz ->
                    val last = attempts.firstOrNull { it.quizId == quiz.id }
                    PnsaCard(onClick = { quiz.id?.let { navigator.push(AppDestination.QuizPlay(it)) } }) {
                        Text(quiz.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Text(excerpt(quiz.description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        MetaRow(
                            listOfNotNull(
                                quiz.nbSecond?.let { "$it sec." },
                                "v${quiz.contentVersion ?: 1}",
                                last?.status,
                            ),
                        )
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun QuizPlayScreen(quizId: Long, onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    var quiz by remember { mutableStateOf<Quiz?>(null) }
    var attempt by remember { mutableStateOf<QuizAttempt?>(null) }
    var index by remember { mutableStateOf(0) }
    val answers = remember { mutableStateMapOf<Long, Long>() }
    var loading by remember { mutableStateOf(true) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var done by remember { mutableStateOf(false) }

    LaunchedEffect(quizId) {
        loading = true
        runCatching {
            val detail = graph.catalog.quiz(quizId)
            val started = graph.quizAttempts.start(quizId)
            detail to started
        }.onSuccess { (detail, started) ->
            quiz = detail.copy(
                questionnaires = detail.questionnaires.sortedBy { it.ordre ?: it.id?.toInt() ?: 0 },
            )
            attempt = started
            graph.profile.track("quiz_start", mapOf("quiz_id" to quizId.toString()))
        }.onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
        loading = false
    }

    val questions = quiz?.questionnaires.orEmpty()
    val current = questions.getOrNull(index)

    Scaffold(topBar = { PnsaTopBar(quiz?.title ?: "Quiz", onBack = onBack) }) { padding ->
        when {
            loading -> LoadingState("Préparation de ta tentative…")
            error != null && quiz == null -> ErrorState(error.orEmpty())
            done && attempt != null && quiz != null -> QuizResultPane(
                quiz = quiz!!,
                attempt = attempt!!,
                answers = answers.toMap(),
                onBack = onBack,
            )
            current == null -> EmptyState("Quiz indisponible", "Aucune question n’est encore publiée.")
            else -> Column(
                Modifier
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text("Question ${index + 1} / ${questions.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinearProgressIndicator(
                    progress = { (index + 1f) / questions.size.coerceAtLeast(1) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                )
                Text(current.prompt, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                current.reponses.forEach { option ->
                    val selected = current.id != null && answers[current.id] == option.id
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                onClick = {
                                    val qid = current.id ?: return@selectable
                                    val rid = option.id ?: return@selectable
                                    answers[qid] = rid
                                    val clientId = attempt?.clientAttemptId
                                    if (clientId != null) {
                                        scope.launch {
                                            runCatching { graph.quizAttempts.saveProgress(clientId, answers.toMap()) }
                                                .onSuccess { attempt = it }
                                        }
                                    }
                                },
                            )
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected, onClick = null)
                        Text(option.reponse.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                error?.let {
                    Spacer(Modifier.height(12.dp))
                    StatusBanner(it)
                }
                Spacer(Modifier.height(24.dp))
                if (index < questions.lastIndex) {
                    PrimaryAction("Question suivante", enabled = current.id != null && answers[current.id] != null) {
                        index += 1
                    }
                    if (index > 0) QuietAction("Question précédente") { index -= 1 }
                } else {
                    PrimaryAction(if (submitting) "Envoi…" else "Terminer et envoyer", enabled = !submitting) {
                        val clientId = attempt?.clientAttemptId ?: return@PrimaryAction
                        scope.launch {
                            submitting = true
                            error = null
                            runCatching { graph.quizAttempts.submit(clientId, questions, answers.toMap()) }
                                .onSuccess {
                                    attempt = it
                                    done = true
                                    graph.profile.track("quiz_submit", mapOf("quiz_id" to quizId.toString()))
                                }
                                .onFailure { throwable ->
                                    error = (throwable as? ApiException)?.userMessage()
                                        ?: "Envoi non confirmé. Ta tentative est conservée."
                                }
                            submitting = false
                        }
                    }
                    SecondaryAction("Abandonner cette tentative") {
                        val clientId = attempt?.clientAttemptId ?: return@SecondaryAction
                        scope.launch {
                            runCatching { graph.quizAttempts.abandon(clientId) }
                            onBack()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizResultPane(
    quiz: Quiz,
    attempt: QuizAttempt,
    answers: Map<Long, Long>,
    onBack: () -> Unit,
) {
    val max = QuizScoring.maxScore(quiz.questionnaires)
    val local = QuizScoring.localScore(quiz.questionnaires, answers)
    val score = attempt.displayScore ?: local
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Résultat", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("$score / $max", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        StatusBanner(
            when (attempt.status) {
                "synced" -> "Score confirmé par le serveur (version ${attempt.quizVersion ?: quiz.contentVersion ?: 1})."
                "completed_local" -> "Terminé localement — en attente d’envoi."
                "sync_error" -> "Erreur de transmission. La tentative est conservée."
                else -> "État : ${attempt.status ?: "enregistré"}."
            },
        )
        Spacer(Modifier.height(16.dp))
        quiz.questionnaires.forEach { question ->
            val selected = question.id?.let { answers[it] }
            val chosen = question.reponses.firstOrNull { it.id == selected }
            val correct = question.reponses.firstOrNull { it.isCorrect }
            PnsaCard(Modifier.padding(bottom = 10.dp)) {
                Text(question.prompt, fontWeight = FontWeight.SemiBold)
                Text("Ta réponse : ${chosen?.reponse ?: "non répondue"}")
                Text(
                    "Bonne réponse : ${correct?.reponse ?: "—"}",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        PrimaryAction("Retour aux quiz", onClick = onBack)
    }
}
