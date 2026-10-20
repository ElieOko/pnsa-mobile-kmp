package app.partners.pnsa.features.structure.ui

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.PnsaChip
import app.partners.pnsa.core.ui.components.quietClick
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.features.structure.data.KinshasaCenters
import app.partners.pnsa.features.structure.data.StructureChatPreview
import app.partners.pnsa.features.structure.domain.models.HealthStructure

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
            includeWithoutMessages = false,
        ).filter { preview ->
            val matches = normalizedQuery.isBlank() || preview.structureName.lowercase().contains(normalizedQuery) || preview.city.lowercase().contains(normalizedQuery)
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

    PnsaScaffold(containerColor = Color(0xFFF4F5F7)) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF4F5F7)),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF075E54))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Messagerie", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
            }
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SearchField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = "Rechercher une structure",
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PnsaChip(label = "Toutes", selected = !onlyUnread, onClick = { onlyUnread = false })
                        PnsaChip(label = "Non lues", selected = onlyUnread, onClick = { onlyUnread = true })
                    }
                    if (query.isNotBlank() && !canSearch) {
                        Text(
                            "Tape au moins 2 caractères pour lancer la recherche.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            if (startedPreviews.isEmpty() && discover.isEmpty()) {
                EmptyState(
                    title = "Aucune conversation",
                    message = "Recherche une structure pour démarrer un chat.",
                )
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                ) {
                    if (startedPreviews.isNotEmpty()) {
                        item {
                            Text(
                                "Discussions",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    items(startedPreviews, key = { it.structureId }) { preview ->
                        ConversationRow(
                            preview = preview,
                            onClick = { navigator.push(AppDestination.StructureChat(preview.structureId)) },
                        )
                    }
                    if (discover.isNotEmpty()) {
                        item {
                            Text(
                                "Résultats de recherche",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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

    PnsaScaffold(containerColor = Color(0xFFECE5DD)) { padding ->
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
                .fillMaxSize(),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF075E54))
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White,
                    modifier = Modifier.quietClick { onBack() },
                )
                Box(
                    Modifier
                        .size(34.dp)
                        .background(Color(0xFF25D366), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(structure.displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    Text(structure.displayName, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("En ligne · ${structure.displayCity}", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White)
                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(messages, key = { it.id }) { message ->
                    val bubbleColor = if (message.fromStructure) Color.White else Color(0xFFDCF8C6)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromStructure) Arrangement.Start else Arrangement.End) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = bubbleColor,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth(0.86f),
                        ) {
                            Column(Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                                Text(message.text)
                                Spacer(Modifier.height(3.dp))
                                Row(
                                    modifier = Modifier.align(Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(
                                        message.timeLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (!message.fromStructure) {
                                        Icon(
                                            imageVector = if (message.read) Icons.Default.DoneAll else Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (message.read) Color(0xFF34B7F1) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                ) {
                    BasicTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        decorationBox = { inner ->
                            if (draft.isBlank()) {
                                Text("Message", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            inner()
                        },
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF25D366),
                ) {
                    IconButton(
                        onClick = {
                            val text = draft.trim()
                            if (text.isBlank()) return@IconButton
                            messages = graph.messaging.sendToStructure(structure, text)
                            draft = ""
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer", tint = Color.White)
                    }
                }
            }
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
        color = Color(0xFFF0F2F5),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                decorationBox = { inner ->
                    if (value.isBlank()) {
                        Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .quietClick(onClick = onClick),
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier
                        .size(46.dp)
                        .background(Color(0xFF25D366), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        preview.structureName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(preview.structureName, fontWeight = FontWeight.SemiBold)
                    Text(
                        preview.lastMessage,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        preview.lastTime.ifBlank { " " },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (preview.unreadCount > 0) Color(0xFF25D366) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (preview.unreadCount > 0) {
                        Box(
                            Modifier
                                .padding(top = 4.dp)
                                .background(Color(0xFF25D366), CircleShape)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                        ) {
                            Text(preview.unreadCount.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE8E8E8))
        }
    }
}
