package app.partners.pnsa.features.home.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import app.partners.pnsa.core.ui.components.PageBackdrop
import app.partners.pnsa.core.ui.components.PnsaScaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.partners.pnsa.resources.Res
import app.partners.pnsa.resources.hero_bg
import org.jetbrains.compose.resources.painterResource
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.network.ApiException
import app.partners.pnsa.core.ui.components.AvatarCircle
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.ErrorState
import app.partners.pnsa.core.ui.components.LoadingState
import app.partners.pnsa.core.ui.components.MetaRow
import app.partners.pnsa.core.ui.components.PnsaCard
import app.partners.pnsa.core.ui.components.SectionTitle
import app.partners.pnsa.core.ui.components.ShortcutTile
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.util.excerpt
import app.partners.pnsa.core.util.formatIsoDate
import app.partners.pnsa.features.content.domain.models.HomeFeed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    val user by graph.session.user.collectAsState()
    val scope = rememberCoroutineScope()
    var feed by remember { mutableStateOf<HomeFeed?>(null) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var offlineHint by remember { mutableStateOf<String?>(null) }

    fun load(fromRefresh: Boolean = false) {
        scope.launch {
            if (fromRefresh) refreshing = true else loading = true
            error = null
            offlineHint = null
            runCatching {
                val synced = runCatching { graph.catalog.syncCatalog() }.getOrNull()
                val home = graph.catalog.home()
                synced to home
            }.onSuccess { (sync, home) ->
                feed = home
                graph.screens.home = home
                graph.screens.homeLoaded = true
                graph.session.saveLastSync(sync?.syncedAt ?: sync?.cursor ?: graph.catalog.lastSyncLabel())
            }.onFailure { throwable ->
                val cached = graph.catalog.cachedContenus()
                if (cached.isNotEmpty()) {
                    feed = HomeFeed(contenus = cached, faqs = graph.catalog.cachedFaqs())
                    graph.screens.home = feed
                    graph.screens.homeLoaded = true
                    offlineHint = "Affichage du dernier catalogue synchronisé."
                } else {
                    error = (throwable as? ApiException)?.userMessage() ?: throwable.message
                }
            }
            loading = false
            refreshing = false
        }
    }

    LaunchedEffect(Unit) {
        val cached = graph.screens.home
        if (graph.screens.homeLoaded && cached != null) {
            feed = cached
            loading = false
        } else {
            load()
        }
    }

    PnsaScaffold { padding ->
        PageBackdrop {
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { load(true) },
            modifier = Modifier.padding(padding).fillMaxSize(),
        ) {
            when {
                loading && feed == null -> LoadingState("Préparation de ton accueil…")
                error != null && feed == null -> ErrorState(error.orEmpty(), onRetry = { load() })
                feed == null -> EmptyState("Rien à afficher", "Tire pour actualiser.")
                else -> {
                    val data = feed!!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(148.dp)
                                    .clip(RoundedCornerShape(24.dp)),
                            ) {
                                Image(
                                    painterResource(Res.drawable.hero_bg),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xE00B3C8A), Color(0x990069E1), Color(0xB3D01D2A)),
                                            ),
                                        ),
                                )
                                Column(
                                    Modifier.align(Alignment.BottomStart).padding(16.dp),
                                ) {
                                    Text("Salut ${user?.prenom ?: ""}", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Text("Plateforme professionnelle SSR · Kinshasa", color = Color.White.copy(alpha = 0.88f))
                                }
                                AvatarCircle(user?.initials ?: "?", Modifier.align(Alignment.TopEnd).padding(14.dp))
                            }
                            Spacer(Modifier.height(10.dp))
                            StatusBanner(
                                "Synchronisé ${formatIsoDate(graph.catalog.lastSyncLabel())}. Un contenu nouveau sur le site apparaît ici sans mettre à jour l’application.",
                            )
                            offlineHint?.let {
                                Spacer(Modifier.height(8.dp))
                                StatusBanner(it)
                            }
                        }
                        item {
                            SectionTitle("Que veux-tu faire ?")
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                ShortcutTile("Apprendre", "Articles et fiches", Icons.Default.Favorite, { navigator.push(AppDestination.Learn) }, Modifier.weight(1f))
                                ShortcutTile("Quiz", "Tester tes connaissances", Icons.Default.Star, { navigator.push(AppDestination.Quizzes) }, Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                ShortcutTile("Structures", "Trouver une structure", Icons.Default.Place, { navigator.push(AppDestination.Structures) }, Modifier.weight(1f))
                                ShortcutTile("Conseil", "Forum et privé", Icons.Default.Help, { navigator.push(AppDestination.Forum) }, Modifier.weight(1f))
                            }
                        }
                        item { SectionTitle("Nouveautés") }
                        if (data.contenus.isEmpty()) {
                            item {
                                EmptyState("Aucun contenu pour le moment", "Reviens après la prochaine publication.")
                            }
                        }
                        items(data.contenus, key = { it.id ?: it.libelle.orEmpty() }) { item ->
                            PnsaCard(onClick = { item.id?.let { navigator.push(AppDestination.ContentDetail(it)) } }) {
                                MetaRow(listOfNotNull(item.categoryLabel, item.langue?.uppercase(), if (item.allowOffline == true) "Hors ligne" else null))
                                Spacer(Modifier.height(8.dp))
                                Text(item.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(excerpt(item.description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Version ${item.contentVersion ?: 1} · ${formatIsoDate(item.updatedAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (data.faqs.isNotEmpty()) {
                            item { SectionTitle("Questions fréquentes") }
                            items(data.faqs.take(4), key = { "faq-${it.id}" }) { faq ->
                                PnsaCard(onClick = { navigator.push(AppDestination.Help) }) {
                                    Text(faq.libelle.orEmpty(), fontWeight = FontWeight.SemiBold)
                                    Text(excerpt(faq.description, 120), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
        }
    }
}
