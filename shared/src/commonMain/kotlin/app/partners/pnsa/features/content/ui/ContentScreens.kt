package app.partners.pnsa.features.content.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import app.partners.pnsa.core.ui.components.MetaRow
import app.partners.pnsa.core.ui.components.PnsaCard
import app.partners.pnsa.core.ui.components.PnsaTextField
import app.partners.pnsa.core.ui.components.PnsaTopBar
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.util.excerpt
import app.partners.pnsa.core.util.formatIsoDate
import app.partners.pnsa.features.content.domain.models.Contenu
import kotlinx.coroutines.launch

@Composable
fun ContentListScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<Contenu>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tous") }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            runCatching { graph.catalog.contenus(perPage = 50) }
                .onSuccess { items = it.data }
                .onFailure { throwable ->
                    items = graph.catalog.cachedContenus()
                    if (items.isEmpty()) {
                        error = (throwable as? ApiException)?.userMessage() ?: throwable.message
                    }
                }
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    val categories = listOf("Tous") + items.map { it.categoryLabel }.distinct()
    val filtered = items.filter { item ->
        val matchesCategory = category == "Tous" || item.categoryLabel == category
        val haystack = listOf(item.libelle, item.description, item.theme, item.categoryLabel).joinToString(" ").lowercase()
        matchesCategory && (query.isBlank() || haystack.contains(query.lowercase()))
    }

    Scaffold(topBar = { PnsaTopBar("Apprendre") }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            PnsaTextField(query, { query = it }, "Rechercher un thème, un mot-clé…")
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { label ->
                    FilterChip(selected = category == label, onClick = { category = label }, label = { Text(label) })
                }
            }
            Spacer(Modifier.height(8.dp))
            when {
                loading -> LoadingState("Chargement du catalogue…")
                error != null -> ErrorState(error.orEmpty(), onRetry = { load() })
                filtered.isEmpty() -> EmptyState(
                    "Aucun contenu",
                    if (query.isNotBlank()) "Aucun résultat pour « $query ». Modifie ta recherche." else "Le catalogue publié est vide pour le moment.",
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtered, key = { it.id ?: it.title }) { item ->
                        PnsaCard(onClick = { item.id?.let { navigator.push(AppDestination.ContentDetail(it)) } }) {
                            MetaRow(listOfNotNull(item.categoryLabel, item.langue?.uppercase(), item.extension?.uppercase()))
                            Spacer(Modifier.height(8.dp))
                            Text(item.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                            Text(excerpt(item.description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun ContentDetailScreen(id: Long, onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    var item by remember { mutableStateOf<Contenu?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        loading = true
        runCatching { graph.catalog.contenu(id) }
            .onSuccess {
                item = it
                graph.profile.track("content_view", mapOf("content_id" to id.toString()))
            }
            .onFailure { throwable ->
                error = (throwable as? ApiException)?.userMessage() ?: throwable.message
            }
        loading = false
    }

    Scaffold(topBar = { PnsaTopBar(item?.title ?: "Contenu", onBack = onBack) }) { padding ->
        when {
            loading -> LoadingState()
            error != null -> ErrorState(error.orEmpty(), onRetry = {
                scope.launch {
                    loading = true
                    error = null
                    runCatching { graph.catalog.contenu(id) }
                        .onSuccess { item = it }
                        .onFailure { error = it.message }
                    loading = false
                }
            })
            item == null -> EmptyState("Contenu introuvable", "Il a peut-être été retiré du catalogue.")
            else -> {
                val content = item!!
                Column(
                    Modifier
                        .padding(padding)
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    MetaRow(
                        listOfNotNull(
                            content.categoryLabel,
                            content.theme,
                            "v${content.contentVersion ?: 1}",
                            content.langue?.uppercase(),
                        ),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Mis à jour le ${formatIsoDate(content.updatedAt)}", style = MaterialTheme.typography.labelMedium)
                    if (content.allowOffline == true) {
                        Spacer(Modifier.height(8.dp))
                        StatusBanner("Consultation hors connexion autorisée pour ce contenu.")
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        content.description.orEmpty().replace("\r\n", "\n"),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (content.filePath.isNullOrBlank().not()) {
                        Spacer(Modifier.height(16.dp))
                        StatusBanner("Un support associé est disponible (${content.extension ?: "fichier"}). Le téléchargement complet n’est confirmé qu’après réception.")
                    }
                }
            }
        }
    }
}
