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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.network.ApiException
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.ErrorState
import app.partners.pnsa.core.ui.components.LoadingState
import app.partners.pnsa.core.ui.components.PageBackdrop
import app.partners.pnsa.core.ui.components.PnsaCard
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.PnsaTopBar
import app.partners.pnsa.core.ui.components.PrimaryAction
import app.partners.pnsa.core.ui.components.QuietAction
import app.partners.pnsa.core.ui.components.SecondaryAction
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.components.quietClick
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.theme.PnsaBlue
import app.partners.pnsa.core.ui.theme.PnsaNavy
import app.partners.pnsa.core.ui.theme.PnsaRed
import app.partners.pnsa.core.util.QuizScoring
import app.partners.pnsa.core.util.excerpt
import app.partners.pnsa.features.quiz.domain.models.Quiz
import app.partners.pnsa.features.quiz.domain.models.QuizAttempt
import app.partners.pnsa.resources.Res
import app.partners.pnsa.resources.quiz_question
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

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

    val lastId = graph.prefs.lastQuizId

    PnsaScaffold(topBar = { PnsaTopBar("Quiz") }) { padding ->
        PageBackdrop {
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
                                .height(132.dp)
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
                            Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                                Text("Quiz SSR", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
                                Text("Tester tes connaissances", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                    }
                    item {
                        StatusBanner("Tu peux interrompre un quiz et le reprendre. Le score définitif est confirmé par le serveur.")
                    }
                    items(quizzes, key = { it.id ?: it.title }) { quiz ->
                        val last = attempts.firstOrNull { it.quizId == quiz.id }
                        QuizCatalogCard(
                            quiz = quiz,
                            attempt = last,
                            highlighted = quiz.id == lastId,
                            onOpen = {
                                quiz.id?.let {
                                    graph.prefs.lastQuizId = it
                                    navigator.push(AppDestination.QuizPlay(it))
                                }
                            },
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun QuizCatalogCard(
    quiz: Quiz,
    attempt: QuizAttempt?,
    highlighted: Boolean,
    onOpen: () -> Unit,
) {
    val score = attempt?.displayScore
    val total = attempt?.totalQuestions ?: quiz.questionCount.takeIf { it > 0 }
    PnsaCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                if (highlighted) {
                    Text("Dernier quiz", color = PnsaBlue, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
                Text(quiz.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium, color = PnsaNavy)
                Spacer(Modifier.height(4.dp))
                Text(excerpt(quiz.description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoPill(quiz.nbSecond?.let { "$it sec." } ?: "Libre")
                    InfoPill("${quiz.questionCount.takeIf { it > 0 } ?: "—"} questions")
                    InfoPill(attempt?.status ?: "Nouveau")
                }
            }
            if (score != null && total != null && total > 0) {
                ScoreBadge(score, total)
            }
        }
    }
}

@Composable
private fun InfoPill(text: String) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(Color(0xFFE8F2FF))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = PnsaBlue)
    }
}

@Composable
private fun ScoreBadge(score: Int, total: Int) {
    Box(
        Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(PnsaBlue.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Text("$score/$total", fontWeight = FontWeight.Bold, color = PnsaBlue, fontSize = 12.sp)
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
    val progress = if (questions.isEmpty()) 0f else (index + 1f) / questions.size

    PnsaScaffold(topBar = { PnsaTopBar(quiz?.title ?: "Quiz", onBack = onBack) }) { padding ->
        PageBackdrop {
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
                    Text("Question ${index + 1} / ${questions.size}", color = PnsaBlue, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = PnsaBlue,
                        trackColor = Color(0xFFE8F2FF),
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(current.prompt, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PnsaNavy)
                    Spacer(Modifier.height(16.dp))
                    current.reponses.forEachIndexed { optionIndex, option ->
                        val selected = current.id != null && answers[current.id] == option.id
                        val letter = ('A' + optionIndex).toString()
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) PnsaBlue.copy(alpha = 0.12f) else Color.White)
                                .quietClick {
                                    val qid = current.id ?: return@quietClick
                                    val rid = option.id ?: return@quietClick
                                    answers[qid] = rid
                                    val clientId = attempt?.clientAttemptId
                                    if (clientId != null) {
                                        scope.launch {
                                            runCatching { graph.quizAttempts.saveProgress(clientId, answers.toMap()) }
                                                .onSuccess { attempt = it }
                                        }
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) PnsaBlue else Color(0xFFE8F2FF)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(letter, color = if (selected) Color.White else PnsaBlue, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.size(12.dp))
                            Text(option.reponse.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = PnsaNavy)
                        }
                    }
                    error?.let {
                        Spacer(Modifier.height(12.dp))
                        StatusBanner(it)
                    }
                    Spacer(Modifier.height(20.dp))
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
        Text("Résultat", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = PnsaNavy)
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(PnsaBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text("$score / $max", fontWeight = FontWeight.Bold, color = PnsaBlue, fontSize = 20.sp)
        }
        Spacer(Modifier.height(12.dp))
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
            val ok = chosen != null && chosen.id == correct?.id
            PnsaCard(Modifier.padding(bottom = 10.dp)) {
                Text(question.prompt, fontWeight = FontWeight.SemiBold, color = PnsaNavy)
                Spacer(Modifier.height(6.dp))
                Text("Ta réponse : ${chosen?.reponse ?: "non répondue"}", color = if (ok) PnsaBlue else PnsaRed)
                Text("Bonne réponse : ${correct?.reponse ?: "—"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        PrimaryAction("Retour aux quiz", onClick = onBack)
    }
}
