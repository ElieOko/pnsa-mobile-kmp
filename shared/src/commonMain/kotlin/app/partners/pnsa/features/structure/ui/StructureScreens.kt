package app.partners.pnsa.features.structure.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import app.partners.pnsa.core.ui.components.PrimaryAction
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.util.formatIsoDate
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import app.partners.pnsa.features.structure.domain.models.Orientation
import kotlinx.coroutines.launch

@Composable
fun StructureListScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<HealthStructure>>(emptyList()) }
    var province by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            runCatching {
                graph.catalog.syncStructures()
                graph.catalog.structures(
                    perPage = 50,
                    province = province.ifBlank { null },
                    city = city.ifBlank { null },
                    queryText = query.ifBlank { null },
                )
            }.onSuccess { items = it.data }
                .onFailure { throwable ->
                    error = (throwable as? ApiException)?.userMessage() ?: throwable.message
                }
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(topBar = { PnsaTopBar("Trouver une structure") }) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            PnsaTextField(query, { query = it }, "Nom ou service")
            Spacer(Modifier.height(8.dp))
            PnsaTextField(province, { province = it }, "Province")
            Spacer(Modifier.height(8.dp))
            PnsaTextField(city, { city = it }, "Ville")
            Spacer(Modifier.height(8.dp))
            PrimaryAction("Rechercher") { load() }
            Spacer(Modifier.height(12.dp))
            when {
                loading -> LoadingState("Recherche des structures…")
                error != null -> ErrorState(error.orEmpty(), onRetry = { load() })
                items.isEmpty() -> EmptyState(
                    "Aucune structure pour ces filtres",
                    "L’annuaire n’affiche que les fiches actives et validées. Tu peux chercher sans géolocalisation, par province ou ville.",
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(items, key = { it.id ?: it.displayName }) { structure ->
                        PnsaCard(onClick = { structure.id?.let { navigator.push(AppDestination.StructureDetail(it)) } }) {
                            Text(structure.displayName, fontWeight = FontWeight.SemiBold)
                            Text("${structure.province ?: "—"} · ${structure.displayCity}")
                            Text(structure.displayAddress, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (structure.hasCoordinates) {
                                Spacer(Modifier.height(6.dp))
                                MetaRow(listOf("GPS ${structure.latitude}, ${structure.longitude}"))
                            }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun StructureDetailScreen(id: Long, navigator: AppNavigator, onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    var item by remember { mutableStateOf<HealthStructure?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        loading = true
        runCatching { graph.catalog.structure(id) }
            .onSuccess {
                item = it
                graph.profile.track("structure_view", mapOf("structure_id" to id.toString()))
            }
            .onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
        loading = false
    }

    Scaffold(topBar = { PnsaTopBar(item?.displayName ?: "Structure", onBack = onBack) }) { padding ->
        when {
            loading -> LoadingState()
            error != null -> ErrorState(error.orEmpty())
            item == null -> EmptyState("Fiche introuvable", "Cette structure n’est plus proposée.")
            else -> {
                val structure = item!!
                Column(
                    Modifier
                        .padding(padding)
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    MetaRow(listOfNotNull(structure.structureType, structure.province, structure.displayCity))
                    Spacer(Modifier.height(12.dp))
                    Text(structure.displayAddress)
                    if (!structure.phone.isNullOrBlank()) Text("Tél. ${structure.phone}")
                    if (!structure.email.isNullOrBlank()) Text(structure.email.orEmpty())
                    if (!structure.openingHours.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Horaires : ${structure.openingHours}")
                    }
                    if (!structure.services.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Services", fontWeight = FontWeight.SemiBold)
                        Text(structure.services.orEmpty())
                    }
                    if (structure.hasCoordinates) {
                        Spacer(Modifier.height(8.dp))
                        StatusBanner("Position indicative : ${structure.latitude}, ${structure.longitude}. Ce n’est pas un temps de trajet.")
                    } else {
                        Spacer(Modifier.height(8.dp))
                        StatusBanner("Coordonnées GPS non renseignées. La fiche reste consultable.")
                    }
                    Spacer(Modifier.height(20.dp))
                    PrimaryAction("Demander une orientation") {
                        navigator.push(AppDestination.OrientationCreate(id))
                    }
                }
            }
        }
    }
}

@Composable
fun OrientationListScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<Orientation>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            runCatching { graph.orientations.list() }
                .onSuccess { items = it.data }
                .onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(topBar = { PnsaTopBar("Mes orientations", onBack = { navigator.pop() }) }) { padding ->
        when {
            loading -> LoadingState()
            error != null -> ErrorState(error.orEmpty(), onRetry = { load() })
            items.isEmpty() -> EmptyState(
                "Aucune orientation",
                "Choisis une structure dans l’annuaire pour créer une référence. Une intention de visite n’est pas un service reçu.",
            )
            else -> LazyColumn(
                Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items, key = { it.id ?: it.clientRequestId.orEmpty() }) { item ->
                    PnsaCard(onClick = { item.id?.let { navigator.push(AppDestination.OrientationDetail(it)) } }) {
                        Text(item.healthStructure?.displayName ?: "Structure", fontWeight = FontWeight.SemiBold)
                        Text(item.statusLabel)
                        Text(item.reason ?: "Sans motif précisé", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun OrientationDetailScreen(id: Long, onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    var item by remember { mutableStateOf<Orientation?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        runCatching { graph.orientations.detail(id) }
            .onSuccess { item = it }
            .onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
        loading = false
    }

    Scaffold(topBar = { PnsaTopBar("Orientation", onBack = onBack) }) { padding ->
        when {
            loading -> LoadingState()
            error != null -> ErrorState(error.orEmpty())
            item == null -> EmptyState("Introuvable", "Cette référence n’est plus accessible.")
            else -> {
                val orientation = item!!
                Column(Modifier.padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
                    Text(orientation.healthStructure?.displayName ?: "Structure", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("État : ${orientation.statusLabel}")
                    Text("Créée le ${formatIsoDate(orientation.createdAt)}")
                    Spacer(Modifier.height(12.dp))
                    Text(orientation.reason ?: "Aucun motif saisi.")
                    Spacer(Modifier.height(16.dp))
                    Text("Historique", fontWeight = FontWeight.SemiBold)
                    if (orientation.events.isEmpty()) {
                        Text("Aucun événement complémentaire pour le moment.")
                    } else {
                        orientation.events.forEach { event ->
                            PnsaCard(Modifier.padding(vertical = 6.dp)) {
                                Text(event.status ?: "Événement", fontWeight = FontWeight.Medium)
                                Text(event.comment ?: "")
                                Text(formatIsoDate(event.createdAt), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrientationCreateScreen(structureId: Long, navigator: AppNavigator, onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    var reason by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { PnsaTopBar("Nouvelle orientation", onBack = onBack) }) { padding ->
        Column(Modifier.padding(padding).padding(20.dp)) {
            StatusBanner("Avant validation, vérifie la structure. Le serveur confirmera la référence. Un double envoi avec le même identifiant ne crée pas de doublon.")
            Spacer(Modifier.height(16.dp))
            PnsaTextField(reason, { reason = it }, "Motif (optionnel)", singleLine = false)
            error?.let {
                Spacer(Modifier.height(12.dp))
                StatusBanner(it)
            }
            Spacer(Modifier.height(20.dp))
            PrimaryAction(if (loading) "Envoi…" else "Confirmer la demande", enabled = !loading) {
                scope.launch {
                    loading = true
                    error = null
                    runCatching { graph.orientations.create(structureId, reason) }
                        .onSuccess { created ->
                            graph.profile.track("orientation_create", mapOf("structure_id" to structureId.toString()))
                            created.id?.let { navigator.replaceCurrent(AppDestination.OrientationDetail(it)) }
                                ?: navigator.replaceCurrent(AppDestination.Orientations)
                        }
                        .onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
                    loading = false
                }
            }
        }
    }
}
