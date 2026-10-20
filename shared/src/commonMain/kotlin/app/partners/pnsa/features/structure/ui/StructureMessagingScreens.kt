package app.partners.pnsa.features.structure.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.InlineBackHeader
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.quietClick
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.theme.PnsaChatCanvas
import app.partners.pnsa.core.ui.theme.PnsaGreenSoft
import app.partners.pnsa.core.ui.theme.PnsaInk
import app.partners.pnsa.core.ui.theme.PnsaMuted
import app.partners.pnsa.core.ui.theme.bagdeColor
import app.partners.pnsa.core.ui.theme.wsColor
import app.partners.pnsa.features.forum.ui.ChatMessageBubble
import app.partners.pnsa.features.forum.ui.WhatsAppComposerBar
import app.partners.pnsa.features.structure.data.KinshasaCenters
import app.partners.pnsa.features.structure.data.StructureChatPreview

@Composable
fun StructureConversationsScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    var query by remember { mutableStateOf("") }
    var onlyUnread by remember { mutableStateOf(false) }
    val structures = remember(graph.screens.structures) {
        graph.screens.structures
            .ifEmpty { KinshasaCenters.all }
            .filter { it.id != null }
            .distinctBy { it.id }
    }
    val normalizedQuery = query.trim().lowercase()
    val canSearch = normalizedQuery.length >= 2
    val startedPreviews = remember(structures, normalizedQuery, onlyUnread) {
        graph.messaging.previews(
            structures = structures,
            includeWithoutMessages = true,
        ).filter { preview ->
            val matches = normalizedQuery.isBlank() ||
                preview.structureName.lowercase().contains(normalizedQuery) ||
                preview.city.lowercase().contains(normalizedQuery)
            val unreadOk = !onlyUnread || preview.unreadCount > 0
            matches && unreadOk
        }
    }
    val startedIds = remember(startedPreviews) { startedPreviews.map { it.structureId }.toSet() }
    val discover = remember(structures, canSearch, normalizedQuery, startedIds) {
        if (!canSearch) {
            emptyList()
        } else {
            structures
                .filter {
                    val id = it.id
                    id != null &&
                        id !in startedIds &&
                        (it.displayName.lowercase().contains(normalizedQuery) || it.displayCity.lowercase().contains(normalizedQuery))
                }
                .take(25)
        }
    }

    PnsaScaffold(containerColor = Color.White) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White),
        ) {
            // WhatsApp-style header (no TopBar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Discussions Structures",
                        color = bagdeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                    Text(
                        text = "Échange direct avec les structures de santé SSR",
                        color = PnsaMuted,
                        fontSize = 12.sp,
                    )
                }
                Surface(
                    modifier = Modifier.quietClick { navigator.push(AppDestination.Advice) },
                    shape = RoundedCornerShape(35.dp),
                    color = wsColor,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Conseil privé",
                            tint = bagdeColor,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Conseil privé",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PnsaInk,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SearchField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Rechercher une structure ou commune",
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = { onlyUnread = false },
                        label = {
                            Text(
                                text = "Toutes (${startedPreviews.size})",
                                fontSize = 12.sp,
                                fontWeight = if (!onlyUnread) FontWeight.Bold else FontWeight.Medium,
                                color = if (!onlyUnread) PnsaInk else PnsaMuted,
                            )
                        },
                        shape = RoundedCornerShape(35.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (!onlyUnread) wsColor else PnsaGreenSoft,
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Color.Transparent,
                        ),
                    )
                    SuggestionChip(
                        onClick = { onlyUnread = true },
                        label = {
                            Text(
                                text = "Non lues",
                                fontSize = 12.sp,
                                fontWeight = if (onlyUnread) FontWeight.Bold else FontWeight.Medium,
                                color = if (onlyUnread) PnsaInk else PnsaMuted,
                            )
                        },
                        shape = RoundedCornerShape(35.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (onlyUnread) wsColor else PnsaGreenSoft,
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Color.Transparent,
                        ),
                    )
                }
                if (query.isNotBlank() && !canSearch) {
                    Text(
                        "Tape au moins 2 caractères pour lancer la recherche.",
                        color = PnsaMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            if (startedPreviews.isEmpty() && discover.isEmpty()) {
                EmptyState(
                    title = "Aucune conversation",
                    message = "Recherche une structure ou sélectionne un centre sur la carte pour démarrer un chat.",
                )
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                ) {
                    items(startedPreviews, key = { it.structureId }) { preview ->
                        ConversationRow(
                            preview = preview,
                            onClick = { navigator.push(AppDestination.StructureChat(preview.structureId)) },
                        )
                    }
                    if (discover.isNotEmpty()) {
                        item {
                            Text(
                                "Autres structures disponibles",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = bagdeColor,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    items(discover, key = { it.id ?: it.displayName }) { structure ->
                        val id = structure.id ?: return@items
                        ConversationRow(
                            preview = StructureChatPreview(
                                structureId = id,
                                structureName = structure.displayName,
                                city = structure.displayCity,
                                lastMessage = "Démarrer la conversation",
                                lastTime = "",
                                unreadCount = 0,
                            ),
                            onClick = { navigator.push(AppDestination.StructureChat(id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StructureChatScreen(structureId: Long, onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    val structures = remember(graph.screens.structures) {
        graph.screens.structures.ifEmpty { KinshasaCenters.all }
    }
    val structure = remember(structureId, structures) {
        structures.firstOrNull { it.id == structureId } ?: KinshasaCenters.byId(structureId)
    }
    var draft by remember { mutableStateOf("") }
    var messages by remember(structure?.id) {
        mutableStateOf(structure?.let { graph.messaging.messagesFor(it) }.orEmpty())
    }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    PnsaScaffold(containerColor = PnsaChatCanvas) { padding ->
        if (structure == null) {
            EmptyState("Structure introuvable", "Retourne à la carte puis réessaie.")
            return@PnsaScaffold
        }
        LaunchedEffect(structure.id) {
            messages = graph.messaging.messagesFor(structure)
        }
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(PnsaChatCanvas),
        ) {
            // Clean white WhatsApp conversation header (no TopBar)
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
            ) {
                Box(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    InlineBackHeader(
                        title = structure.displayName,
                        subtitle = "En ligne · ${structure.displayCity}",
                        onBack = onBack,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageBubble(
                        senderName = if (message.fromStructure) structure.displayName else "Toi",
                        messageText = message.text,
                        timeText = message.timeLabel,
                        isSentByMe = !message.fromStructure,
                        statusRead = message.read,
                    )
                }
            }

            WhatsAppComposerBar(
                value = draft,
                onValueChange = { draft = it },
                placeholder = "Écrire un message à la structure…",
                sending = false,
                onSend = {
                    val text = draft.trim()
                    if (text.isBlank()) return@WhatsAppComposerBar
                    messages = graph.messaging.sendToStructure(structure, text)
                    draft = ""
                },
            )
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, bagdeColor.copy(alpha = 0.25f)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = bagdeColor, modifier = Modifier.size(18.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = PnsaInk, fontSize = 14.sp),
                cursorBrush = SolidColor(bagdeColor),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (value.isBlank()) {
                        Text(placeholder, color = PnsaMuted, fontSize = 14.sp)
                    }
                    inner()
                },
            )
        }
    }
}

@Composable
private fun ConversationRow(
    preview: StructureChatPreview,
    onClick: () -> Unit,
) {
    val initials = preview.structureName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
        .ifEmpty { "ST" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .quietClick(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(wsColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initials,
                        color = bagdeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preview.structureName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PnsaInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${preview.city} · ${preview.lastMessage}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 13.sp,
                        color = PnsaMuted,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = preview.lastTime.ifBlank { "En ligne" },
                    fontSize = 11.sp,
                    color = if (preview.unreadCount > 0) bagdeColor else PnsaMuted,
                    fontWeight = if (preview.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                )
                if (preview.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(bagdeColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = preview.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 78.dp, end = 14.dp),
            color = Color(0xFFEEF3EE),
        )
    }
}
