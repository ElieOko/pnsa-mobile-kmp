package app.partners.pnsa.features.forum.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.config.AppConfig
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.network.ApiException
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.ErrorState
import app.partners.pnsa.core.ui.components.LoadingState
import app.partners.pnsa.core.ui.components.PageBackdrop
import app.partners.pnsa.core.ui.components.PnsaCard
import app.partners.pnsa.core.ui.components.PnsaChip
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.PnsaTextField
import app.partners.pnsa.core.ui.components.PnsaTopBar
import app.partners.pnsa.core.ui.components.PrimaryAction
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.theme.PnsaBlue
import app.partners.pnsa.core.ui.theme.PnsaNavy
import app.partners.pnsa.core.ui.theme.PnsaRed
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
    var category by remember { mutableStateOf(graph.prefs.forumCategory) }

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

    val cachedCategories = graph.catalog.cachedCategories()
    val categories = remember(items, cachedCategories) {
        val fromPosts = items.map { it.categoryLabel }.filter { it.isNotBlank() }
        val fromSync = cachedCategories.mapNotNull { it.libelle }
        (listOf("Tous") + (fromSync + fromPosts)).distinct()
    }
    val filtered = if (category == "Tous") items else items.filter { it.categoryLabel == category }

    PnsaScaffold(topBar = { PnsaTopBar("Forum") }) { padding ->
        PageBackdrop {
            when {
                loading -> LoadingState()
                error != null -> ErrorState(error.orEmpty(), onRetry = { load() })
                items.isEmpty() -> EmptyState("Aucun sujet publié", "Les questions validées par la modération apparaîtront ici.")
                else -> LazyColumn(
                    Modifier.padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text("Discussions publiques", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PnsaNavy)
                        Text("Catégories, auteurs et réponses — hors urgence médicale.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { label ->
                                PnsaChip(
                                    label = label,
                                    selected = category == label,
                                    onClick = {
                                        category = label
                                        graph.prefs.forumCategory = label
                                    },
                                )
                            }
                        }
                    }
                    item {
                        StatusBanner("Le forum n’est pas un service d’urgence. Signalement : ${AppConfig.contactEmail}.")
                    }
                    items(filtered, key = { it.id ?: it.headline }) { sujet ->
                        ForumPostCard(sujet) { sujet.id?.let { navigator.push(AppDestination.ForumDetail(it)) } }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ForumPostCard(sujet: Sujet, onOpen: () -> Unit) {
    PnsaCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(PnsaBlue.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    sujet.categoryLabel,
                    color = PnsaBlue,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.weight(1f))
            MetaIcon(Icons.Outlined.Schedule, formatIsoDate(sujet.publishedAt))
        }
        Spacer(Modifier.height(10.dp))
        Text(sujet.headline, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium, color = PnsaNavy)
        Spacer(Modifier.height(4.dp))
        Text(excerpt(sujet.description, 160), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            AuthorMark(sujet.authorName)
            Spacer(Modifier.weight(1f))
            MetaIcon(Icons.Outlined.ChatBubbleOutline, "${sujet.replyCount}")
            MetaIcon(Icons.Outlined.FavoriteBorder, "${sujet.likeCount}")
        }
    }
}

@Composable
private fun AuthorMark(name: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier.size(22.dp).clip(CircleShape).background(PnsaRed.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(name.take(1).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PnsaRed)
        }
        Text(name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MetaIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        PageBackdrop {
            when {
                loading -> LoadingState()
                error != null && sujet == null -> ErrorState(error.orEmpty(), onRetry = { load() })
                sujet == null -> EmptyState("Sujet indisponible", "Il a peut-être été retiré.")
                else -> {
                    val post = sujet!!
                    Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
                        PnsaCard {
                            Text(post.categoryLabel, color = PnsaBlue, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text(post.headline, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PnsaNavy)
                            Spacer(Modifier.height(8.dp))
                            Text(post.description.orEmpty())
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                AuthorMark(post.authorName)
                                Text(formatIsoDate(post.publishedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${post.replyCount} réponses", style = MaterialTheme.typography.labelSmall, color = PnsaBlue)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text("Réponses (${post.commentaires.size})", fontWeight = FontWeight.SemiBold, color = PnsaNavy)
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (post.commentaires.isEmpty()) {
                                item {
                                    Text("Aucune réponse pour le moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            items(post.commentaires, key = { it.id ?: it.commentaire.orEmpty() }) { comment ->
                                Row {
                                    Box(
                                        Modifier
                                            .padding(end = 10.dp, top = 4.dp)
                                            .width(3.dp)
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(PnsaBlue.copy(alpha = 0.35f)),
                                    )
                                    PnsaCard(Modifier.weight(1f)) {
                                        AuthorMark(comment.user?.displayName ?: "Participant")
                                        Spacer(Modifier.height(6.dp))
                                        Text(comment.commentaire.orEmpty())
                                        Spacer(Modifier.height(6.dp))
                                        Text(formatIsoDate(comment.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
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
        PageBackdrop {
            when {
                loading -> LoadingState()
                error != null -> ErrorState(error.orEmpty())
                items.isEmpty() -> EmptyState(
                    "Aucune discussion pour l’instant",
                    "Un conseiller habilité ouvrira la conversation. Ce n’est pas un canal d’urgence. Contact PNSA : ${AppConfig.contactPhone}.",
                )
                else -> LazyColumn(
                    Modifier.padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items, key = { it.id ?: it.title }) { discussion ->
                        PnsaCard(onClick = { discussion.id?.let { navigator.push(AppDestination.AdviceChat(it)) } }) {
                            Text(discussion.title, fontWeight = FontWeight.SemiBold, color = PnsaNavy)
                            Text("Mis à jour ${formatIsoDate(discussion.updatedAt ?: discussion.createdAt)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
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
                            Text(if (mine) "Toi" else "Conseiller", fontWeight = FontWeight.Medium, color = if (mine) PnsaBlue else PnsaNavy)
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
