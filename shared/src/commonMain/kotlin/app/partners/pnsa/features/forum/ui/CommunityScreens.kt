package app.partners.pnsa.features.forum.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.config.AppConfig
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.LoadingState
import app.partners.pnsa.core.ui.components.PageBackdrop
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.components.quietClick
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.theme.PnsaChatCanvas
import app.partners.pnsa.core.ui.theme.PnsaGreenBubbleSent
import app.partners.pnsa.core.ui.theme.PnsaGreenSoft
import app.partners.pnsa.core.ui.theme.PnsaInk
import app.partners.pnsa.core.ui.theme.PnsaMuted
import app.partners.pnsa.core.ui.theme.bagdeColor
import app.partners.pnsa.core.ui.theme.wsColor
import app.partners.pnsa.core.util.excerpt
import app.partners.pnsa.core.util.formatIsoDate
import app.partners.pnsa.features.forum.domain.models.Categorie
import app.partners.pnsa.features.forum.domain.models.Discussion
import app.partners.pnsa.features.forum.domain.models.Message
import app.partners.pnsa.features.forum.domain.models.Sujet
import app.partners.pnsa.features.user.domain.models.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoxDiscussionRow(
    sujet: Sujet,
    isSelect: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val unreadCount = sujet.replyCount.coerceAtLeast(1)
    val lastComment = sujet.commentaires.lastOrNull()
    val previewMessage = lastComment?.commentaire ?: excerpt(sujet.description, 60).ifBlank { sujet.headline }
    val timeLabel = formatShortTime(sujet.publishedAt)
    val sentByMe = lastComment?.user?.id == 1L

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .background(if (isSelect) wsColor else Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.padding(end = 12.dp)) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(60))
                        .background(wsColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = sujet.authorName.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = bagdeColor,
                    )
                }
                if (isSelect) {
                    Badge(
                        containerColor = bagdeColor,
                        modifier = Modifier
                            .size(22.dp)
                            .absoluteOffset(y = 28.dp, x = 28.dp)
                            .border(
                                width = 2.dp,
                                color = Color(0xF788F18A),
                                shape = CircleShape,
                            ),
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = sujet.headline,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PnsaInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PnsaGreenSoft,
                    ) {
                        Text(
                            text = sujet.categoryLabel,
                            color = bagdeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sentByMe) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = bagdeColor,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp),
                        )
                    }
                    Text(
                        text = previewMessage,
                        color = Color.Black.copy(alpha = 0.55f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = timeLabel,
                color = if (unreadCount > 0) bagdeColor else Color.Black.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = if (unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
            )
            Spacer(Modifier.height(6.dp))
            Badge(
                modifier = Modifier.size(22.dp),
                containerColor = bagdeColor,
            ) {
                Text(
                    text = unreadCount.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    senderName: String,
    messageText: String,
    timeText: String,
    isSentByMe: Boolean,
    statusRead: Boolean? = true,
) {
    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isSentByMe) 18.dp else 4.dp,
        bottomEnd = if (isSentByMe) 4.dp else 18.dp,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = if (isSentByMe) Alignment.End else Alignment.Start,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(min = 90.dp, max = 300.dp)
                .clip(shape)
                .clipToBounds(),
            shape = shape,
            color = if (isSentByMe) PnsaGreenBubbleSent else Color.White,
            border = BorderStroke(
                width = 1.dp,
                color = if (isSentByMe) bagdeColor.copy(alpha = 0.28f) else Color(0xFFE2ECE3),
            ),
            shadowElevation = 1.dp,
        ) {
            Column(
                Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clipToBounds(),
            ) {
                if (!isSentByMe && senderName.isNotBlank()) {
                    Text(
                        text = senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = bagdeColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Text(
                    text = messageText,
                    color = PnsaInk,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = timeText,
                        color = Color.Black.copy(alpha = 0.45f),
                        fontSize = 10.sp,
                    )
                    if (isSentByMe) {
                        Icon(
                            imageVector = if (statusRead == true) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (statusRead == true) bagdeColor else Color.Black.copy(alpha = 0.45f),
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsAppComposerBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Écrire un message…",
    sending: Boolean = false,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clipToBounds()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 44.dp, max = 120.dp)
                .clip(RoundedCornerShape(24.dp))
                .clipToBounds(),
            shape = RoundedCornerShape(24.dp),
            color = PnsaChatCanvas,
            border = BorderStroke(1.dp, bagdeColor.copy(alpha = 0.30f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    maxLines = 4,
                    cursorBrush = SolidColor(bagdeColor),
                    textStyle = TextStyle(color = PnsaInk, fontSize = 15.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clipToBounds(),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clipToBounds(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = PnsaMuted,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = {
                if (value.isNotBlank() && !sending) {
                    onSend()
                }
            },
            modifier = Modifier.size(44.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = Color.White,
                containerColor = bagdeColor,
            ),
        ) {
            Icon(
                imageVector = if (value.isBlank()) Icons.Default.Mic else Icons.Default.Send,
                contentDescription = "Envoyer",
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun InlineConversationHeader(
    title: String,
    subtitle: String,
    initials: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(36.dp).quietClick(onClick = onBack),
            shape = CircleShape,
            color = wsColor,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = bagdeColor, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(60))
                .background(bagdeColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PnsaInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = bagdeColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AdviceListScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    var items by remember { mutableStateOf<List<Discussion>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val fallbackDiscussions = remember {
        listOf(
            Discussion(id = 1L, libelle = "Conseiller SSR · Hilaire", updatedAt = "22:10"),
            Discussion(id = 2L, libelle = "Ligne d’orientation · Gedeon", updatedAt = "19:45"),
            Discussion(id = 3L, libelle = "Accompagnement confidentiel", updatedAt = "15:12"),
        )
    }

    LaunchedEffect(Unit) {
        runCatching { graph.community.discussions() }
            .onSuccess { items = it.data.ifEmpty { fallbackDiscussions } }
            .onFailure { items = fallbackDiscussions }
        loading = false
    }

    PnsaScaffold { padding ->
        PageBackdrop {
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White),
            ) {
                InlineConversationHeader(
                    title = "Échanges confidentiels",
                    subtitle = "Avec un conseiller SSR",
                    initials = "EC",
                    onBack = { navigator.pop() },
                )
                HorizontalDivider(color = bagdeColor.copy(alpha = 0.15f))

                when {
                    loading -> LoadingState()
                    items.isEmpty() -> EmptyState(
                        "Aucune discussion pour l’instant",
                        "Un conseiller habilité ouvrira la conversation. Contact PNSA : ${AppConfig.contactPhone}.",
                    )
                    else -> LazyColumn(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clipToBounds(),
                    ) {
                        items(items, key = { it.id ?: it.title.hashCode().toLong() }) { discussion ->
                            val discId = discussion.id ?: 1L
                            BoxDiscussionRow(
                                sujet = Sujet(
                                    id = discId,
                                    title = discussion.title,
                                    description = "Échange confidentiel · Appuie pour ouvrir la conversation",
                                    categorie = Categorie(libelle = "Privé"),
                                    user = User(prenom = discussion.title.take(2)),
                                    commentairesCount = 1,
                                    updatedAt = discussion.updatedAt ?: discussion.createdAt ?: "12:13",
                                ),
                                onClick = { navigator.push(AppDestination.AdviceChat(discId)) },
                            )
                            HorizontalDivider(
                                color = Color(0xFFF0F4F0),
                                modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                            )
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
    var value by rememberSaveable(id) { mutableStateOf(graph.prefs.getString("advice_draft_$id").orEmpty()) }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val fallbackMessages = remember(id) {
        val savedJson = graph.prefs.getString("advice_msgs_$id")
        val initial = if (!savedJson.isNullOrBlank()) {
            runCatching {
                app.partners.pnsa.core.network.AppJson.decodeFromString(
                    kotlinx.serialization.builtins.ListSerializer(Message.serializer()),
                    savedJson,
                )
            }.getOrNull()
        } else {
            null
        } ?: listOf(
            Message(
                id = 1L,
                msg = "Bonjour ! Bienvenue sur l’espace d’échange confidentiel du PNSA. Comment puis-je t’orienter aujourd’hui ?",
                userId = 99L,
                createdAt = "15:10",
                status = true,
            ),
            Message(
                id = 2L,
                msg = "Tes échanges restent strictement privés.",
                userId = 99L,
                createdAt = "15:12",
                status = true,
            ),
        )
        mutableStateListOf<Message>().apply { addAll(initial) }
    }

    fun persistAdviceMessages(list: List<Message>) {
        runCatching {
            val encoded = app.partners.pnsa.core.network.AppJson.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(Message.serializer()),
                list,
            )
            graph.prefs.putString("advice_msgs_$id", encoded)
        }
    }

    fun load() {
        scope.launch {
            loading = true
            runCatching { graph.community.messages(id) }
                .onSuccess {
                    val data = it.data.ifEmpty { fallbackMessages }
                    messages = data
                    persistAdviceMessages(data)
                }
                .onFailure { messages = fallbackMessages.toList() }
            loading = false
        }
    }

    LaunchedEffect(id) { load() }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    PnsaScaffold(containerColor = PnsaChatCanvas) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .background(PnsaChatCanvas),
        ) {
            InlineConversationHeader(
                title = "Conseiller SSR #$id",
                subtitle = "En ligne · Échange confidentiel",
                initials = "CS",
                onBack = onBack,
            )
            HorizontalDivider(color = bagdeColor.copy(alpha = 0.15f))

            if (loading) {
                Box(Modifier.weight(1f).fillMaxWidth()) { LoadingState() }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clipToBounds(),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 6.dp),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        item {
                            Box(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                                StatusBanner("Les notifications n’affichent jamais le texte de tes messages.")
                            }
                        }
                        items(messages, key = { it.id ?: it.msg.orEmpty().hashCode().toLong() }) { message ->
                            val mine = (message.userId != null && message.userId == user?.id) || message.userId == 1L
                            ChatMessageBubble(
                                senderName = if (mine) "Toi" else "Conseiller SSR",
                                messageText = message.msg.orEmpty(),
                                timeText = formatShortTime(message.createdAt),
                                isSentByMe = mine,
                                statusRead = message.status,
                            )
                        }
                    }
                }
            }

            WhatsAppComposerBar(
                value = value,
                onValueChange = {
                    value = it
                    graph.prefs.putString("advice_draft_$id", it.ifBlank { null })
                },
                placeholder = "Écrire un message…",
                sending = sending,
                onSend = {
                    val trimmed = value.trim()
                    if (trimmed.isEmpty()) return@WhatsAppComposerBar
                    scope.launch {
                        sending = true
                        runCatching { graph.community.sendMessage(id, trimmed) }
                            .onSuccess {
                                value = ""
                                graph.prefs.putString("advice_draft_$id", null)
                                load()
                            }
                            .onFailure {
                                val newMsg = Message(
                                    id = (messages.size + 100).toLong(),
                                    msg = trimmed,
                                    userId = user?.id ?: 1L,
                                    createdAt = "12:13",
                                    status = true,
                                )
                                fallbackMessages.add(newMsg)
                                val updated = fallbackMessages.toList()
                                messages = updated
                                persistAdviceMessages(updated)
                                value = ""
                                graph.prefs.putString("advice_draft_$id", null)
                            }
                        sending = false
                    }
                },
            )
        }
    }
}

private fun formatShortTime(raw: String?): String {
    if (raw.isNullOrBlank()) return "12:13"
    if (raw.length <= 6 && raw.contains(":")) return raw
    return formatIsoDate(raw).ifBlank { "12:13" }
}
