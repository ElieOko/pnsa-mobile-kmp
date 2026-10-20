package app.partners.pnsa.features.forum.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.config.AppConfig
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.network.ApiException
import app.partners.pnsa.core.ui.components.ChatBubble
import app.partners.pnsa.core.ui.components.DiscussionAvatar
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.ErrorState
import app.partners.pnsa.core.ui.components.LoadingState
import app.partners.pnsa.core.ui.components.MessageComposer
import app.partners.pnsa.core.ui.components.PnsaChip
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.PnsaTopBar
import app.partners.pnsa.core.ui.components.SelectionActionBar
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.components.UnreadBadge
import app.partners.pnsa.core.ui.components.quietClick
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.theme.BadgeColor
import app.partners.pnsa.core.ui.theme.PnsaInk
import app.partners.pnsa.core.ui.theme.WsChatBackground
import app.partners.pnsa.core.ui.theme.WsColor
import app.partners.pnsa.core.ui.theme.WsGreenDark
import app.partners.pnsa.core.util.excerpt
import app.partners.pnsa.core.util.formatIsoDate
import app.partners.pnsa.features.forum.domain.models.Discussion
import app.partners.pnsa.features.forum.domain.models.Message
import app.partners.pnsa.features.forum.domain.models.Sujet
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ForumListScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<Sujet>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var category by remember { mutableStateOf(graph.prefs.forumCategory) }
    // État façon WhatsApp : discussions ouvertes (lues), favorites et sélection multiple.
    val openedIds = remember { mutableStateListOf<Long>() }
    val favoriteIds = remember { mutableStateListOf<Long>() }
    val selectedIds = remember { mutableStateListOf<Long>() }
    var filter by remember { mutableStateOf(DiscussionFilter.Toutes) }

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
    val unreadCount = items.count { (it.id ?: -1L) !in openedIds }
    val byFilter = when (filter) {
        DiscussionFilter.Toutes -> items
        DiscussionFilter.NonLues -> items.filter { (it.id ?: -1L) !in openedIds }
        DiscussionFilter.Favoris -> items.filter { (it.id ?: -1L) in favoriteIds }
        DiscussionFilter.Groupes -> items.filter { it.categoryLabel.contains("group", ignoreCase = true) }
    }
    val filtered = if (category == "Tous") byFilter else byFilter.filter { it.categoryLabel == category }
    val selectionMode = selectedIds.isNotEmpty()

    fun toggleSelected(sujet: Sujet) {
        val id = sujet.id ?: return
        if (id in selectedIds) selectedIds.remove(id) else selectedIds.add(id)
    }

    fun openDiscussion(sujet: Sujet) {
        if (selectionMode) {
            toggleSelected(sujet)
        } else {
            sujet.id?.let { id ->
                if (id !in openedIds) openedIds.add(id)
                navigator.push(AppDestination.ForumDetail(id))
            }
        }
    }

    PnsaScaffold { padding ->
        Box(Modifier.padding(padding).fillMaxSize().background(Color.White)) {
            Column(Modifier.fillMaxSize()) {
                AnimatedVisibility(visible = selectionMode) {
                    SelectionActionBar(
                        count = selectedIds.size,
                        onClose = { selectedIds.clear() },
                        onToggleFavorite = {
                            selectedIds.forEach { id ->
                                if (id in favoriteIds) favoriteIds.remove(id) else favoriteIds.add(id)
                            }
                            selectedIds.clear()
                        },
                    )
                }
                if (!selectionMode) {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Spacer(Modifier.height(14.dp))
                        Text("Discussions", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = WsGreenDark)
                        Text("Forum public PNSA — hors urgence médicale.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                    }
                }
                // Chips de filtre : Toutes / Non lues / Favoris / Groupes (+ catégories).
                LazyRow(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(DiscussionFilter.entries, key = { it.name }) { entry ->
                        val label = when (entry) {
                            DiscussionFilter.NonLues -> if (unreadCount > 0) "Non lues $unreadCount" else "Non lues"
                            else -> entry.label
                        }
                        PnsaChip(label = label, selected = filter == entry, onClick = { if (!selectionMode) filter = entry })
                    }
                    items(categories.drop(1), key = { "cat-$it" }) { label ->
                        PnsaChip(
                            label = label,
                            selected = category == label,
                            onClick = {
                                if (!selectionMode) {
                                    category = label
                                    graph.prefs.forumCategory = label
                                }
                            },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                when {
                    loading -> LoadingState()
                    error != null && items.isEmpty() -> ErrorState(error.orEmpty(), onRetry = { load() })
                    filtered.isEmpty() -> EmptyState(
                        title = when (filter) {
                            DiscussionFilter.NonLues -> "Aucune discussion non lue"
                            DiscussionFilter.Favoris -> "Aucun favori"
                            DiscussionFilter.Groupes -> "Aucun groupe"
                            DiscussionFilter.Toutes -> "Aucun sujet publié"
                        },
                        message = when (filter) {
                            DiscussionFilter.Favoris -> "Appuie long sur une discussion, puis touche l'étoile pour l'ajouter aux favoris."
                            DiscussionFilter.Groupes -> "Les discussions de groupe apparaîtront ici."
                            else -> "Les questions validées par la modération apparaîtront ici."
                        },
                    )
                    else -> LazyColumn(Modifier.weight(1f)) {
                        item {
                            StatusBanner(
                                "Le forum n’est pas un service d’urgence. Signalement : ${AppConfig.contactEmail}.",
                                Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                        }
                        items(filtered, key = { it.id ?: it.headline }) { sujet ->
                            DiscussionRow(
                                sujet = sujet,
                                unread = (sujet.id ?: -1L) !in openedIds,
                                favorite = (sujet.id ?: -1L) in favoriteIds,
                                selected = (sujet.id ?: -1L) in selectedIds,
                                onClick = { openDiscussion(sujet) },
                                onLongClick = { toggleSelected(sujet) },
                                modifier = Modifier.animateItem(),
                            )
                            HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(start = 82.dp))
                        }
                        item { Spacer(Modifier.height(88.dp)) }
                    }
                }
            }
            FloatingActionButton(
                onClick = { navigator.push(AppDestination.Advice) },
                containerColor = BadgeColor,
                contentColor = Color.White,
                modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp),
            ) {
                Icon(Icons.Filled.Chat, contentDescription = "Nouvelle discussion privée", modifier = Modifier.size(25.dp))
            }
        }
    }
}

private enum class DiscussionFilter(val label: String) {
    Toutes("Toutes"),
    NonLues("Non lues"),
    Favoris("Favoris"),
    Groupes("Groupes"),
}

/** Ligne de discussion façon WhatsApp : avatar, titre gras, aperçu, heure verte, badge non-lus. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DiscussionRow(
    sujet: Sujet,
    unread: Boolean,
    favorite: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(if (selected) WsColor.copy(alpha = 0.45f) else Color.White)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DiscussionAvatar(sujet.authorName, selected = selected)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    sujet.headline,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PnsaInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (favorite) {
                    Spacer(Modifier.width(5.dp))
                    Icon(Icons.Default.Star, contentDescription = "Favori", tint = BadgeColor, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                excerpt(sujet.description, 80).ifBlank { sujet.categoryLabel },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(sujet.authorName, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                formatIsoDate(sujet.publishedAt),
                fontSize = 11.sp,
                fontWeight = if (unread) FontWeight.Bold else FontWeight.Normal,
                color = if (unread) BadgeColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (unread) UnreadBadge(maxOf(sujet.replyCount, 1))
        }
    }
}

@Composable
fun ForumDetailScreen(id: Long, onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    val user by graph.session.user.collectAsState()
    val scope = rememberCoroutineScope()
    var sujet by remember { mutableStateOf<Sujet?>(null) }
    var draft by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

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

    val commentCount = sujet?.commentaires?.size ?: 0
    LaunchedEffect(commentCount, loading) {
        if (!loading && commentCount > 0) {
            listState.animateScrollToItem(commentCount - 1)
        }
    }

    PnsaScaffold(topBar = { PnsaTopBar(sujet?.headline ?: "Discussion", onBack = onBack) }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().background(WsChatBackground)) {
            when {
                loading -> LoadingState()
                error != null && sujet == null -> ErrorState(error.orEmpty(), onRetry = { load() })
                sujet == null -> EmptyState("Sujet indisponible", "Il a peut-être été retiré.")
                else -> {
                    val post = sujet!!
                    Column(Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            item {
                                Spacer(Modifier.height(10.dp))
                                // Message d'ouverture de la discussion.
                                ChatBubble(
                                    text = post.description.orEmpty().ifBlank { post.headline },
                                    mine = post.userId != null && post.userId == user?.id,
                                    author = post.authorName,
                                    time = formatIsoDate(post.publishedAt),
                                    read = true,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${post.categoryLabel} · ${post.replyCount} réponse${if (post.replyCount > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 6.dp),
                                )
                            }
                            if (post.commentaires.isEmpty()) {
                                item {
                                    Text(
                                        "Aucune réponse pour le moment. Écris la première ci-dessous.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(8.dp),
                                    )
                                }
                            }
                            items(post.commentaires, key = { it.id ?: it.commentaire.orEmpty() }) { comment ->
                                val mine = comment.userId != null && comment.userId == user?.id
                                ChatBubble(
                                    text = comment.commentaire.orEmpty(),
                                    mine = mine,
                                    author = comment.user?.displayName ?: "Participant",
                                    time = formatIsoDate(comment.createdAt),
                                    read = comment.status ?: true,
                                    modifier = Modifier.animateItem(),
                                )
                            }
                            item { Spacer(Modifier.height(6.dp)) }
                        }
                        MessageComposer(
                            value = draft,
                            onValueChange = { draft = it },
                            placeholder = "Répondre…",
                            sending = sending,
                            onSend = {
                                val text = draft.trim()
                                if (text.isBlank()) return@MessageComposer
                                scope.launch {
                                    sending = true
                                    runCatching { graph.community.comment(id, text) }
                                        .onSuccess {
                                            draft = ""
                                            load()
                                        }
                                        .onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
                                    sending = false
                                }
                            },
                        )
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
        Box(Modifier.padding(padding).fillMaxSize().background(Color.White)) {
            when {
                loading -> LoadingState()
                error != null -> ErrorState(error.orEmpty())
                items.isEmpty() -> EmptyState(
                    "Aucune discussion pour l’instant",
                    "Un conseiller habilité ouvrira la conversation. Ce n’est pas un canal d’urgence. Contact PNSA : ${AppConfig.contactPhone}.",
                )
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(items, key = { it.id ?: it.title }) { discussion ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .quietClick(onClick = { discussion.id?.let { navigator.push(AppDestination.AdviceChat(it)) } })
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DiscussionAvatar(discussion.title)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(discussion.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PnsaInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    "Conseil privé et confidentiel",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                )
                            }
                            Text(
                                formatIsoDate(discussion.updatedAt ?: discussion.createdAt),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(start = 82.dp))
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
    val listState = rememberLazyListState()

    fun load() {
        scope.launch {
            loading = true
            runCatching { graph.community.messages(id) }
                .onSuccess { messages = it.data }
            loading = false
        }
    }

    LaunchedEffect(id) { load() }
    LaunchedEffect(messages.size, loading) {
        if (!loading && messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    PnsaScaffold(topBar = { PnsaTopBar("Discussion", onBack = onBack) }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().background(WsChatBackground)) {
            Column(Modifier.fillMaxSize()) {
                StatusBanner(
                    "Échange confidentiel : les notifications n’affichent jamais le texte de tes messages.",
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
                if (loading) {
                    LoadingState()
                } else if (messages.isEmpty()) {
                    EmptyState("Aucun message", "Écris le premier message ci-dessous, un conseiller te répondra.")
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(messages, key = { it.id ?: it.msg.orEmpty() }) { message ->
                            val mine = message.userId != null && message.userId == user?.id
                            ChatBubble(
                                text = message.msg.orEmpty(),
                                mine = mine,
                                author = if (mine) null else "Conseiller",
                                time = formatIsoDate(message.createdAt),
                                read = if (mine) (message.status ?: false) else null,
                                modifier = Modifier.animateItem(),
                            )
                        }
                        item { Spacer(Modifier.height(6.dp)) }
                    }
                }
                MessageComposer(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = "Écrire un message",
                    sending = sending,
                    onSend = {
                        val text = draft.trim()
                        if (text.isBlank()) return@MessageComposer
                        scope.launch {
                            sending = true
                            runCatching { graph.community.sendMessage(id, text) }
                                .onSuccess {
                                    draft = ""
                                    load()
                                }
                            sending = false
                        }
                    },
                )
            }
        }
    }
}
