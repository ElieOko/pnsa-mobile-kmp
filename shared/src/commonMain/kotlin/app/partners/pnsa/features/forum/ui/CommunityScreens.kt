package app.partners.pnsa.features.forum.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import app.partners.pnsa.core.ui.components.PnsaScaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.network.ApiException
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.ErrorState
import app.partners.pnsa.core.ui.components.LoadingState
import app.partners.pnsa.core.ui.components.PnsaCard
import app.partners.pnsa.core.ui.components.PnsaTextField
import app.partners.pnsa.core.ui.components.PnsaTopBar
import app.partners.pnsa.core.ui.components.PrimaryAction
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.util.excerpt
import app.partners.pnsa.core.util.formatIsoDate
import app.partners.pnsa.features.forum.domain.models.Discussion
import app.partners.pnsa.features.forum.domain.models.Message
import app.partners.pnsa.features.forum.domain.models.Sujet
import kotlinx.coroutines.launch

@Composable
fun ForumListScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<Sujet>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            runCatching { graph.community.sujets() }
                .onSuccess {
                    items = it.data
                    graph.screens.sujets = it.data
                    graph.screens.sujetsLoaded = true
                }
                .onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        if (graph.screens.sujetsLoaded) {
            items = graph.screens.sujets
            loading = false
        } else {
            load()
        }
    }

    PnsaScaffold(topBar = { PnsaTopBar("Forum", onBack = { navigator.pop() }) }) { padding ->
        when {
            loading -> LoadingState()
            error != null -> ErrorState(error.orEmpty(), onRetry = { load() })
            items.isEmpty() -> EmptyState("Aucun sujet publié", "Les questions validées par la modération apparaîtront ici.")
            else -> LazyColumn(
                Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    StatusBanner("Le forum n’est pas un service d’urgence. Signale un contenu inapproprié à ${app.partners.pnsa.core.config.AppConfig.contactEmail}.")
                }
                items(items, key = { it.id ?: it.headline }) { sujet ->
                    PnsaCard(onClick = { sujet.id?.let { navigator.push(AppDestination.ForumDetail(it)) } }) {
                        Text(sujet.headline, fontWeight = FontWeight.SemiBold)
                        Text(excerpt(sujet.description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ForumDetailScreen(id: Long, onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    var sujet by remember { mutableStateOf<Sujet?>(null) }
    var draft by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            runCatching { graph.community.sujet(id) }
                .onSuccess { sujet = it }
                .onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
            loading = false
        }
    }

    LaunchedEffect(id) { load() }

    PnsaScaffold(topBar = { PnsaTopBar(sujet?.headline ?: "Sujet", onBack = onBack) }) { padding ->
        when {
            loading -> LoadingState()
            error != null && sujet == null -> ErrorState(error.orEmpty(), onRetry = { load() })
            sujet == null -> EmptyState("Sujet indisponible", "Il a peut-être été retiré.")
            else -> Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
                Text(sujet!!.description.orEmpty())
                Spacer(Modifier.height(12.dp))
                Text("Réponses", fontWeight = FontWeight.SemiBold)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sujet!!.commentaires, key = { it.id ?: it.commentaire.orEmpty() }) { comment ->
                        PnsaCard {
                            Text(comment.user?.displayName ?: "Participant", fontWeight = FontWeight.Medium)
                            Text(comment.commentaire.orEmpty())
                            Text(formatIsoDate(comment.createdAt), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                PnsaTextField(draft, { draft = it }, "Ton commentaire")
                Spacer(Modifier.height(8.dp))
                PrimaryAction(if (sending) "Publication…" else "Publier", enabled = !sending && draft.isNotBlank()) {
                    scope.launch {
                        sending = true
                        runCatching { graph.community.comment(id, draft.trim()) }
                            .onSuccess {
                                draft = ""
                                load()
                            }
                            .onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
                        sending = false
                    }
                }
            }
        }
    }
}

@Composable
fun AdviceListScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    var items by remember { mutableStateOf<List<Discussion>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching { graph.community.discussions() }
            .onSuccess { items = it.data }
            .onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
        loading = false
    }

    PnsaScaffold(topBar = { PnsaTopBar("Conseil privé", onBack = { navigator.pop() }) }) { padding ->
        when {
            loading -> LoadingState()
            error != null -> ErrorState(error.orEmpty())
            items.isEmpty() -> EmptyState(
                "Aucune discussion pour l’instant",
                "Un conseiller habilité ouvrira la conversation. Ce n’est pas un canal d’urgence. Contact PNSA : ${app.partners.pnsa.core.config.AppConfig.contactPhone}.",
            )
            else -> LazyColumn(
                Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items, key = { it.id ?: it.title }) { discussion ->
                    PnsaCard(onClick = { discussion.id?.let { navigator.push(AppDestination.AdviceChat(it)) } }) {
                        Text(discussion.title, fontWeight = FontWeight.SemiBold)
                        Text(formatIsoDate(discussion.updatedAt ?: discussion.createdAt))
                    }
                }
            }
        }
    }
}

@Composable
fun AdviceChatScreen(id: Long, onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    val user by graph.session.user.collectAsState()
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }

    fun load() {
        scope.launch {
            loading = true
            runCatching { graph.community.messages(id) }
                .onSuccess { messages = it.data }
            loading = false
        }
    }

    LaunchedEffect(id) { load() }

    PnsaScaffold(topBar = { PnsaTopBar("Discussion", onBack = onBack) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            StatusBanner("Les notifications n’affichent jamais le texte de tes messages.")
            if (loading) {
                LoadingState()
            } else {
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(messages, key = { it.id ?: it.msg.orEmpty() }) { message ->
                        val mine = message.userId != null && message.userId == user?.id
                        PnsaCard {
                            Text(if (mine) "Toi" else "Conseiller", fontWeight = FontWeight.Medium)
                            Text(message.msg.orEmpty())
                            Text(formatIsoDate(message.createdAt), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            PnsaTextField(draft, { draft = it }, "Écrire un message")
            Spacer(Modifier.height(8.dp))
            PrimaryAction(if (sending) "Envoi…" else "Envoyer", enabled = !sending && draft.isNotBlank()) {
                scope.launch {
                    sending = true
                    runCatching { graph.community.sendMessage(id, draft.trim()) }
                        .onSuccess {
                            draft = ""
                            load()
                        }
                    sending = false
                }
            }
        }
    }
}
