package app.partners.pnsa.features.quiz.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.network.ApiException
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.ErrorState
import app.partners.pnsa.core.ui.components.InlineBackHeader
import app.partners.pnsa.core.ui.components.LoadingState
import app.partners.pnsa.core.ui.components.PageBackdrop
import app.partners.pnsa.core.ui.components.PnsaCard
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.PrimaryAction
import app.partners.pnsa.core.ui.components.QuietAction
import app.partners.pnsa.core.ui.components.SecondaryAction
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.components.quietClick
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.theme.PnsaGreenDeep
import app.partners.pnsa.core.ui.theme.PnsaGreenSoft
import app.partners.pnsa.core.ui.theme.PnsaInk
import app.partners.pnsa.core.ui.theme.PnsaMuted
import app.partners.pnsa.core.ui.theme.bagdeColor
import app.partners.pnsa.core.ui.theme.wsColor
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

    val lastId = remember { graph.prefs.lastQuizId }

    PnsaScaffold { padding ->
        PageBackdrop {
            when {
                loading -> LoadingState("Chargement des quiz…")
                error != null -> ErrorState(error.orEmpty(), onRetry = { load() })
                quizzes.isEmpty() -> EmptyState("Aucun quiz publié", "Les quiz validés par le PNSA apparaîtront ici.")
                else -> LazyColumn(
                    Modifier.padding(padding).fillMaxSize().background(Color.White).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Spacer(Modifier.height(6.dp))
                        InlineBackHeader(
                            title = "Quiz SSR",
                            subtitle = "Évalue et renforce tes connaissances",
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(1.dp, bagdeColor.copy(alpha = 0.25f)),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .background(Brush.horizontalGradient(listOf(bagdeColor, PnsaGreenDeep)))
                                    .padding(18.dp),
                            ) {
                                Column {
                                    Text("Évaluation interactive", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Tester tes connaissances SSR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
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
                                    if (graph.prefs.lastQuizId != it) {
                                        graph.prefs.lastQuizQuestionIndex = 0
                                    }
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
                    Text("Dernier quiz", color = bagdeColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Text(quiz.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = PnsaInk)
                Spacer(Modifier.height(4.dp))
                Text(excerpt(quiz.description), color = PnsaMuted)
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
            .background(wsColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = PnsaInk, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ScoreBadge(score: Int, total: Int) {
    Box(
        Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(wsColor),
        contentAlignment = Alignment.Center,
    ) {
        Text("$score/$total", fontWeight = FontWeight.Bold, color = bagdeColor, fontSize = 12.sp)
    }
}

@Composable
fun QuizPlayScreen(quizId: Long, onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    var quiz by remember { mutableStateOf<Quiz?>(null) }
    var attempt by remember { mutableStateOf<QuizAttempt?>(null) }
    var index by remember { mutableStateOf(if (graph.prefs.lastQuizId == quizId) graph.prefs.lastQuizQuestionIndex else 0) }
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
            val last = detail.questionnaires.size - 1
            if (last >= 0 && graph.prefs.lastQuizId == quizId) {
                index = graph.prefs.lastQuizQuestionIndex.coerceIn(0, last)
            } else {
                index = 0
                graph.prefs.lastQuizQuestionIndex = 0
            }
            graph.prefs.lastQuizId = quizId
        }.onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
        loading = false
    }

    val questions = quiz?.questionnaires.orEmpty()
    val current = questions.getOrNull(index)
    val progress = if (questions.isEmpty()) 0f else (index + 1f) / questions.size

    PnsaScaffold { padding ->
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
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    InlineBackHeader(
                        title = quiz?.title ?: "Quiz",
                        subtitle = "Question ${index + 1} sur ${questions.size}",
                        onBack = onBack,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Question ${index + 1} / ${questions.size}", color = bagdeColor, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text("${answers.size} répondues", style = MaterialTheme.typography.labelMedium, color = PnsaMuted)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = bagdeColor,
                        trackColor = PnsaGreenSoft,
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(current.prompt, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PnsaInk)
                    Spacer(Modifier.height(16.dp))
                    current.reponses.forEachIndexed { optionIndex, option ->
                        val selected = current.id != null && answers[current.id] == option.id
                        val letter = ('A' + optionIndex).toString()
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) wsColor else Color.White)
                                .border(1.dp, if (selected) bagdeColor else Color(0xFFD5E4D6), RoundedCornerShape(16.dp))
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
                                    .background(if (selected) bagdeColor else PnsaGreenSoft),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(letter, color = if (selected) Color.White else bagdeColor, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.size(12.dp))
                            Text(option.reponse.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = PnsaInk)
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
                            graph.prefs.lastQuizId = quizId
                            graph.prefs.lastQuizQuestionIndex = index
                        }
                        if (index > 0) {
                            QuietAction("Question précédente") {
                                index -= 1
                                graph.prefs.lastQuizQuestionIndex = index
                            }
                        }
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
                                        graph.prefs.lastQuizQuestionIndex = 0
                                        graph.profile.track("quiz_submit", mapOf("quiz_id" to quizId.toString()))
                                    }
                                    .onFailure { throwable ->
                                        error = (throwable as? ApiException)?.userMessage()
                                            ?: "Envoi non confirmé. Ta tentative est conservée."
                                    }
                                submitting = false
                            }
                        }
                        if (index > 0) {
                            QuietAction("Question précédente") {
                                index -= 1
                                graph.prefs.lastQuizQuestionIndex = index
                            }
                        }
                        SecondaryAction("Abandonner cette tentative") {
                            val clientId = attempt?.clientAttemptId ?: return@SecondaryAction
                            scope.launch {
                                runCatching { graph.quizAttempts.abandon(clientId) }
                                graph.prefs.lastQuizQuestionIndex = 0
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
    val percent = if (max > 0) (score * 100) / max else 0
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Résultat", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = PnsaInk)
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(wsColor),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$score / $max", fontWeight = FontWeight.Bold, color = bagdeColor, fontSize = 22.sp)
                Text("$percent %", style = MaterialTheme.typography.labelMedium, color = PnsaMuted)
            }
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
            PnsaCard(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Text(question.prompt, fontWeight = FontWeight.Bold, color = PnsaInk)
                Spacer(Modifier.height(6.dp))
                Text("Ta réponse : ${chosen?.reponse ?: "non répondue"}", color = if (ok) bagdeColor else Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold)
                Text("Bonne réponse : ${correct?.reponse ?: "—"}", color = PnsaMuted)
            }
        }
        PrimaryAction("Retour aux quiz", onClick = onBack)
    }
}
