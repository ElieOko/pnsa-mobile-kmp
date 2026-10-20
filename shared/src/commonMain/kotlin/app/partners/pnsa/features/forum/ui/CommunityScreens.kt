package app.partners.pnsa.features.forum.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.config.AppConfig
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.network.ApiException
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.ErrorState
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
import app.partners.pnsa.features.forum.domain.models.Commentaire
import app.partners.pnsa.features.forum.domain.models.Discussion
import app.partners.pnsa.features.forum.domain.models.Message
import app.partners.pnsa.features.forum.domain.models.Sujet
import app.partners.pnsa.features.user.domain.models.User
import kotlinx.coroutines.launch

data class TypeDiscussionFilter(
    val id: Int,
    val title: String,
    val isActive: Boolean = false,
)

data class ConversationItemModel(
    val id: Int,
    val senderName: String,
    val receiverName: String,
    val message: String,
    val time: String = "12:13",
    val status: Boolean? = null,
    val isSentByMe: Boolean = false,
)

private val defaultSampleSujets = listOf(
    Sujet(
        id = 101L,
        title = "Dr. Hilaire · Conseiller SSR",
        description = "Bonjour, quelles sont les méthodes contraceptives adaptées pour les jeunes ?",
        likes = "5",
        categorie = Categorie(libelle = "Santé SSR"),
        user = User(id = 2L, prenom = "Hilaire", nom = "M."),
        commentaires = listOf(
            Commentaire(
                id = 1L,
                commentaire = "Bonjour ! Plusieurs options existent et sont expliquées gratuitement dans nos centres partenaires.",
                user = User(id = 2L, prenom = "Hilaire", nom = "M."),
                createdAt = "22:10",
            ),
        ),
        commentairesCount = 2,
        updatedAt = "22:10",
    ),
    Sujet(
        id = 102L,
        title = "Gedeon · Orientation Kinshasa",
        description = "Bonjour position du centre amical le plus proche à Gombe ?",
        likes = "3",
        categorie = Categorie(libelle = "Orientation"),
        user = User(id = 3L, prenom = "Gedeon", nom = "K."),
        commentaires = listOf(
            Commentaire(
                id = 2L,
                commentaire = "Tu peux consulter l’onglet Carte pour visualiser l’itinéraire direct vers le centre.",
                user = User(id = 1L, prenom = "Professionnel", nom = "PNSA"),
                createdAt = "19:45",
            ),
        ),
        commentairesCount = 1,
        updatedAt = "19:45",
    ),
    Sujet(
        id = 103L,
        title = "David · Dépistage volontaire",
        description = "Le dépistage est-il confidentiel et gratuit pour les adolescents ?",
        likes = "8",
        categorie = Categorie(libelle = "Prévention"),
        user = User(id = 4L, prenom = "David", nom = "B."),
        commentaires = emptyList(),
        commentairesCount = 3,
        updatedAt = "16:30",
    ),
    Sujet(
        id = 104L,
        title = "Terence · Suivi médical",
        description = "Jambo, merci pour les fiches pratiques sur l’hygiène et la prévention.",
        likes = "4",
        categorie = Categorie(libelle = "Groupes"),
        user = User(id = 5L, prenom = "Terence", nom = "L."),
        commentaires = emptyList(),
        commentairesCount = 2,
        updatedAt = "15:12",
    ),
)

@Composable
fun ForumListScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    val scrollHorizontal = rememberScrollState()
    var items by remember { mutableStateOf<List<Sujet>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val selectedIds = remember { mutableStateListOf<Long>() }
    val favoriteIds = remember { mutableStateListOf<Long>(101L) }
    val positionChannel = remember { mutableIntStateOf(1) }
    val activeChip = remember { mutableStateOf(true) }

    val typeDiscussionList = remember {
        mutableStateListOf(
            TypeDiscussionFilter(id = 1, title = "Toutes", isActive = true),
            TypeDiscussionFilter(id = 2, title = "Non lues "),
            TypeDiscussionFilter(id = 3, title = "Favoris "),
            TypeDiscussionFilter(id = 4, title = "Groupes "),
            TypeDiscussionFilter(id = 5, title = "Conseil privé"),
        )
    }

    fun load() {
        scope.launch {
            loading = true
            error = null
            runCatching { graph.community.sujets() }
                .onSuccess {
                    val data = it.data.ifEmpty { defaultSampleSujets }
                    items = data
                    graph.screens.sujets = data
                    graph.screens.sujetsLoaded = true
                }
                .onFailure {
                    items = defaultSampleSujets
                    graph.screens.sujets = defaultSampleSujets
                    graph.screens.sujetsLoaded = true
                }
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        if (graph.screens.sujetsLoaded && graph.screens.sujets.isNotEmpty()) {
            items = graph.screens.sujets
            loading = false
        } else {
            load()
        }
    }

    val allDiscussions = items.ifEmpty { defaultSampleSujets }
    val nonLuesDiscussions = remember(allDiscussions) {
        allDiscussions.filter { (it.replyCount > 0) || (it.id ?: 0L) % 2L == 1L }
    }
    val favorisDiscussions = remember(allDiscussions, favoriteIds.size) {
        allDiscussions.filter { (it.id ?: -1L) in favoriteIds || it.likeCount >= 4 }
    }
    val groupesDiscussions = remember(allDiscussions) {
        allDiscussions.filter { it.categoryLabel.isNotBlank() }
    }

    val displayedDiscussions = when (positionChannel.intValue) {
        1 -> allDiscussions
        2 -> nonLuesDiscussions
        3 -> favorisDiscussions
        4 -> groupesDiscussions
        else -> allDiscussions
    }

    PnsaScaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigator.push(AppDestination.Advice) },
                containerColor = bagdeColor,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(
                    Icons.Default.AddComment,
                    contentDescription = "Conseil privé",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
    ) { padding ->
        PageBackdrop {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White),
            ) {
                // Header or selection bar (without using a global TopAppBar)
                if (selectedIds.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(wsColor)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    selectedIds.clear()
                                    activeChip.value = true
                                },
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Annuler", tint = PnsaInk)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${selectedIds.size} sélectionnée(s)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PnsaInk,
                            )
                        }
                        Text(
                            "Effacer",
                            color = bagdeColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.quietClick {
                                selectedIds.clear()
                                activeChip.value = true
                            },
                        )
                    }
                } else {
                    activeChip.value = true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                "Discussions",
                                color = bagdeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                            )
                            Text(
                                "Espace d’échange SSR & Conseil privé",
                                color = PnsaMuted,
                                fontSize = 12.sp,
                            )
                        }
                        Surface(
                            modifier = Modifier.quietClick { navigator.push(AppDestination.Advice) },
                            shape = RoundedCornerShape(20.dp),
                            color = wsColor,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = bagdeColor, modifier = Modifier.size(16.dp))
                                Text("Privé", color = PnsaInk, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Filter chips row matching DiscussionBody
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollHorizontal)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Spacer(Modifier.width(15.dp))
                    typeDiscussionList.forEachIndexed { indice, it ->
                        SuggestionChip(
                            enabled = activeChip.value,
                            shape = RoundedCornerShape(35.dp),
                            label = {
                                Text(
                                    text = when (it.id) {
                                        2 -> it.title + nonLuesDiscussions.size.toString()
                                        else -> it.title
                                    },
                                    fontWeight = if (it.isActive) FontWeight.Bold else FontWeight.Medium,
                                )
                            },
                            onClick = {
                                if (it.id == 5) {
                                    navigator.push(AppDestination.Advice)
                                } else {
                                    typeDiscussionList.forEachIndexed { i, chip ->
                                        typeDiscussionList[i] = chip.copy(isActive = i == indice)
                                    }
                                    positionChannel.intValue = it.id
                                }
                            },
                            colors = if (it.isActive) {
                                SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = wsColor,
                                    labelColor = if (!activeChip.value) wsColor else PnsaInk,
                                    disabledContainerColor = if (!activeChip.value) wsColor else Color.Unspecified,
                                )
                            } else {
                                SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color.White,
                                    labelColor = PnsaMuted,
                                )
                            },
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                }

                Spacer(Modifier.height(6.dp))

                when {
                    loading -> LoadingState()
                    error != null && displayedDiscussions.isEmpty() -> ErrorState(error.orEmpty(), onRetry = { load() })
                    displayedDiscussions.isEmpty() -> EmptyState(
                        "Aucune discussion",
                        "Aucune discussion dans ce filtre pour le moment.",
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(displayedDiscussions, key = { it.id ?: it.headline.hashCode().toLong() }) { sujet ->
                            val itemId = sujet.id ?: sujet.headline.hashCode().toLong()
                            val isSelected = itemId in selectedIds
                            BoxDiscussionRow(
                                sujet = sujet,
                                isSelect = isSelected,
                                onLongClick = {
                                    if (selectedIds.isEmpty()) {
                                        activeChip.value = false
                                        selectedIds.add(itemId)
                                    }
                                },
                                onClick = {
                                    if (selectedIds.isNotEmpty()) {
                                        if (isSelected) {
                                            selectedIds.remove(itemId)
                                            if (selectedIds.isEmpty()) activeChip.value = true
                                        } else {
                                            selectedIds.add(itemId)
                                        }
                                    } else {
                                        navigator.push(AppDestination.ForumDetail(itemId))
                                    }
                                },
                            )
                            HorizontalDivider(
                                color = Color(0xFFF0F4F0),
                                modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                            )
                        }
                        item {
                            Spacer(Modifier.height(12.dp))
                            Box(Modifier.padding(horizontal = 16.dp)) {
                                StatusBanner("Le forum n’est pas un service d’urgence. Signalement : ${AppConfig.contactEmail}.")
                            }
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = if (isSentByMe) Alignment.End else Alignment.Start,
    ) {
        BoxWithConstraints {
            val bubbleMaxWidth = maxWidth * 0.82f
            Surface(
                modifier = Modifier.widthIn(min = 90.dp, max = bubbleMaxWidth),
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isSentByMe) 18.dp else 4.dp,
                    bottomEnd = if (isSentByMe) 4.dp else 18.dp,
                ),
                color = if (isSentByMe) PnsaGreenBubbleSent else Color.White,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSentByMe) bagdeColor.copy(alpha = 0.28f) else Color(0xFFE2ECE3),
                ),
                shadowElevation = 1.dp,
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    if (!isSentByMe && senderName.isNotBlank()) {
                        Text(
                            text = senderName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = bagdeColor,
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
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(26.dp),
            color = PnsaChatCanvas,
            border = BorderStroke(1.dp, bagdeColor.copy(alpha = 0.30f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    maxLines = 4,
                    textStyle = TextStyle(color = PnsaInk, fontSize = 15.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 20.dp, max = 96.dp),
                )
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = PnsaMuted,
                        fontSize = 14.sp,
                    )
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        IconButton(
            onClick = {
                if (value.isNotBlank() && !sending) {
                    onSend()
                }
            },
            modifier = Modifier.size(46.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = Color.White,
                containerColor = bagdeColor,
            ),
        ) {
            Icon(
                imageVector = if (value.isBlank()) Icons.Default.Mic else Icons.Default.Send,
                contentDescription = "Envoyer",
                modifier = Modifier.size(22.dp),
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
fun ForumDetailScreen(id: Long, onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    val currentUser by graph.session.user.collectAsState()
    val scope = rememberCoroutineScope()
    var sujet by remember { mutableStateOf<Sujet?>(null) }
    var draft by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val localAddedComments = remember { mutableStateListOf<Commentaire>() }

    fun load() {
        scope.launch {
            loading = true
            val sample = defaultSampleSujets.firstOrNull { it.id == id }
            runCatching { graph.community.sujet(id) }
                .onSuccess { sujet = it }
                .onFailure {
                    if (sample != null) {
                        sujet = sample
                    } else {
                        error = (it as? ApiException)?.userMessage() ?: it.message
                    }
                }
            loading = false
        }
    }

    LaunchedEffect(id) { load() }

    PnsaScaffold { padding ->
        when {
            loading -> LoadingState()
            error != null && sujet == null -> ErrorState(error.orEmpty(), onRetry = { load() })
            sujet == null -> EmptyState("Sujet indisponible", "Il a peut-être été retiré.")
            else -> {
                val post = sujet!!
                val allComments = post.commentaires + localAddedComments
                Column(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(PnsaChatCanvas),
                ) {
                    InlineConversationHeader(
                        title = post.headline,
                        subtitle = "${post.categoryLabel} · ${post.authorName}",
                        initials = post.authorName.take(2).uppercase(),
                        onBack = onBack,
                    )
                    HorizontalDivider(color = bagdeColor.copy(alpha = 0.15f))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        // Initial topic message bubble (received)
                        item {
                            ChatMessageBubble(
                                senderName = post.authorName,
                                messageText = post.description?.ifBlank { post.headline } ?: post.headline,
                                timeText = formatShortTime(post.publishedAt),
                                isSentByMe = false,
                            )
                        }

                        if (allComments.isEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Surface(shape = RoundedCornerShape(14.dp), color = Color.White) {
                                        Text(
                                            "Aucune réponse pour le moment. Envoie le premier message !",
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            color = PnsaMuted,
                                            fontSize = 12.sp,
                                        )
                                    }
                                }
                            }
                        }

                        items(allComments, key = { it.id ?: it.commentaire.orEmpty().hashCode().toLong() }) { comment ->
                            val isMe = (comment.userId != null && comment.userId == currentUser?.id) ||
                                comment.user?.id == 1L
                            ChatMessageBubble(
                                senderName = comment.user?.displayName ?: "Participant",
                                messageText = comment.commentaire.orEmpty(),
                                timeText = formatShortTime(comment.createdAt),
                                isSentByMe = isMe,
                                statusRead = true,
                            )
                        }
                    }

                    WhatsAppComposerBar(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = "Répondre dans la discussion…",
                        sending = sending,
                        onSend = {
                            val textToSend = draft.trim()
                            if (textToSend.isEmpty()) return@WhatsAppComposerBar
                            scope.launch {
                                sending = true
                                runCatching { graph.community.comment(id, textToSend) }
                                    .onSuccess {
                                        draft = ""
                                        load()
                                    }
                                    .onFailure {
                                        localAddedComments.add(
                                            Commentaire(
                                                id = (localAddedComments.size + 5000).toLong(),
                                                commentaire = textToSend,
                                                user = currentUser ?: User(id = 1L, prenom = "Moi"),
                                                userId = currentUser?.id ?: 1L,
                                                createdAt = "À l’instant",
                                            ),
                                        )
                                        draft = ""
                                    }
                                sending = false
                            }
                        },
                    )
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
                    title = "Conseil privé",
                    subtitle = "Échanges confidentiels avec un conseiller SSR",
                    initials = "CP",
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
                        Modifier.fillMaxSize(),
                    ) {
                        items(items, key = { it.id ?: it.title.hashCode().toLong() }) { discussion ->
                            val discId = discussion.id ?: 1L
                            BoxDiscussionRow(
                                sujet = Sujet(
                                    id = discId,
                                    title = discussion.title,
                                    description = "Échange privé confidentiel · Appuie pour ouvrir la conversation",
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
    var value by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }

    val fallbackMessages = remember(id) { mutableStateListOf<Message>() }

    fun load() {
        scope.launch {
            loading = true
            runCatching { graph.community.messages(id) }
                .onSuccess { messages = it.data.ifEmpty { fallbackMessages } }
                .onFailure { messages = fallbackMessages }
            loading = false
        }
    }

    LaunchedEffect(id) { load() }

    PnsaScaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(PnsaChatCanvas),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom,
        ) {
            InlineConversationHeader(
                title = "Conseiller SSR #$id",
                subtitle = "En ligne · Échange confidentiel",
                initials = "CS",
                onBack = onBack,
            )
            HorizontalDivider(color = bagdeColor.copy(alpha = 0.15f))

            Box(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                StatusBanner("Les notifications n’affichent jamais le texte de tes messages.")
            }

            if (loading) {
                Box(Modifier.weight(1f)) { LoadingState() }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
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

            WhatsAppComposerBar(
                value = value,
                onValueChange = { value = it },
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
                                messages = fallbackMessages.toList()
                                value = ""
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
