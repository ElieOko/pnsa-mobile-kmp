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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Chat
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
import app.partners.pnsa.core.ui.components.PnsaTextField
import app.partners.pnsa.core.ui.components.quietClick
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.features.structure.data.KinshasaCenters
import app.partners.pnsa.features.structure.domain.models.HealthStructure

@Composable
fun StructureConversationsScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    var query by remember { mutableStateOf("") }
    val structures = remember(graph.screens.structures) {
        graph.screens.structures
            .ifEmpty { KinshasaCenters.all }
            .filter { it.id != null }
            .distinctBy { it.id }
    }
    val previews = remember(structures, query) {
        graph.messaging.previews(structures).filter { preview ->
            if (query.isBlank()) return@filter true
            val needle = query.trim().lowercase()
            preview.structureName.lowercase().contains(needle) || preview.city.lowercase().contains(needle)
        }
    }

    PnsaScaffold(containerColor = Color(0xFFF6F8FB)) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 12.dp),
        ) {
            Spacer(Modifier.height(10.dp))
            Text("Messagerie structures", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Discute directement avec la structure concernée.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            PnsaTextField(
                value = query,
                onValueChange = { query = it },
                label = "Rechercher une structure",
            )
            Spacer(Modifier.height(8.dp))
            if (previews.isEmpty()) {
                EmptyState(
                    title = "Aucune conversation",
                    message = "Choisis une structure depuis la carte ou cherche-la ici pour démarrer.",
                    icon = Icons.Default.Chat,
                )
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(previews, key = { it.structureId }) { preview ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .quietClick { navigator.push(AppDestination.StructureChat(preview.structureId)) },
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    Modifier
                                        .size(44.dp)
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
                                        "${preview.city} · ${preview.lastMessage}",
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        preview.lastTime.ifBlank { "—" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (preview.unreadCount > 0) {
                                        Box(
                                            Modifier
                                                .padding(top = 4.dp)
                                                .background(Color(0xFF25D366), CircleShape)
                                                .padding(horizontal = 7.dp, vertical = 2.dp),
                                        ) {
                                            Text(
                                                preview.unreadCount.toString(),
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
                Text("◀", color = Color.White, modifier = Modifier.quietClick { onBack() })
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
                    val alignment = if (message.fromStructure) Alignment.Start else Alignment.End
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromStructure) Arrangement.Start else Arrangement.End) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = bubbleColor,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth(0.86f),
                        ) {
                            Column(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), horizontalAlignment = alignment) {
                                Text(message.text)
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    message.timeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
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
                                Text("Écrire un message", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
