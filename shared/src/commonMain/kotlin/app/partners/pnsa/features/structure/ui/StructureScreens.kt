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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import app.partners.pnsa.core.ui.components.PnsaChip
import app.partners.pnsa.core.ui.components.PnsaScaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.partners.pnsa.core.ui.components.quietClick
import app.partners.pnsa.core.ui.theme.PnsaBlue
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.location.LatLngPoint
import app.partners.pnsa.core.location.RouteTrack
import app.partners.pnsa.core.location.locationUpdates
import app.partners.pnsa.core.location.rememberLocationGranted
import app.partners.pnsa.core.location.shouldRefreshRoute
import app.partners.pnsa.core.network.ApiException
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.ErrorState
import app.partners.pnsa.core.ui.components.GpsPlaceCard
import app.partners.pnsa.core.ui.components.KinshasaMapMath
import app.partners.pnsa.core.ui.components.LoadingState
import app.partners.pnsa.core.ui.components.MetaRow
import app.partners.pnsa.core.ui.components.PageBackdrop
import app.partners.pnsa.core.ui.components.PnsaCard
import app.partners.pnsa.core.ui.components.PnsaTextField
import app.partners.pnsa.core.ui.components.PnsaTopBar
import app.partners.pnsa.core.ui.components.PrimaryAction
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.map.PlatformStructureMap
import app.partners.pnsa.core.ui.map.usesGoogleMaps
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.util.formatIsoDate
import app.partners.pnsa.features.structure.data.KinshasaCenters
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import app.partners.pnsa.features.structure.domain.models.Orientation
import kotlinx.coroutines.launch

@Composable
fun StructureListScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf(graph.screens.structures.ifEmpty { KinshasaCenters.all }) }
    var province by remember { mutableStateOf("Kinshasa") }
    var city by remember { mutableStateOf(graph.prefs.mapCity) }
    var query by remember { mutableStateOf(graph.prefs.mapQuery) }
    var loading by remember { mutableStateOf(!graph.screens.structuresLoaded) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedId by remember { mutableStateOf(graph.prefs.mapSelectedId ?: items.firstOrNull()?.id) }
    var showMap by remember { mutableStateOf(true) }

    fun apply(list: List<HealthStructure>) {
        val merged = KinshasaCenters.mergeWith(list)
        items = merged
        graph.screens.structures = merged
        graph.screens.structuresLoaded = true
        if (selectedId == null || merged.none { it.id == selectedId }) {
            selectedId = merged.firstOrNull { it.hasCoordinates }?.id
            graph.prefs.mapSelectedId = selectedId
        }
    }

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
            }.onSuccess { apply(it.data) }
                .onFailure { throwable ->
                    apply(emptyList())
                    error = (throwable as? ApiException)?.userMessage()
                }
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        if (graph.screens.structuresLoaded) {
            items = graph.screens.structures
            loading = false
        } else {
            load()
        }
    }

    val filtered = items.filter { item ->
        val haystack = listOf(item.displayName, item.displayCity, item.displayAddress, item.services, item.structureType)
            .joinToString(" ")
            .lowercase()
        val matchesQuery = query.isBlank() || haystack.contains(query.lowercase())
        val matchesCity = city.isBlank() || item.displayCity.contains(city, ignoreCase = true)
        val matchesProvince = province.isBlank() || item.province.orEmpty().contains(province, ignoreCase = true)
        matchesQuery && matchesCity && matchesProvince
    }
    val selected = filtered.firstOrNull { it.id == selectedId }
    val navigation = rememberMapNavigation(selected)
    var followUser by remember { mutableStateOf(true) }

    PnsaScaffold(topBar = { PnsaTopBar("Trouver une structure") }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (showMap) {
                PlatformStructureMap(
                    structures = filtered,
                    selectedId = selectedId,
                    onSelect = { marker ->
                        selectedId = marker.id
                        graph.prefs.mapSelectedId = marker.id
                    },
                    modifier = Modifier.fillMaxSize(),
                    userLocation = navigation.user,
                    route = navigation.route,
                    followUser = followUser && navigation.granted,
                    onFollowInterrupted = { followUser = false },
                )
                Column(
                    Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .padding(12.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PnsaChip(label = "Carte", selected = true, onClick = {})
                        PnsaChip(label = "Liste", selected = false, onClick = {
                            showMap = false
                            graph.prefs.mapShowMap = false
                        })
                    }
                    Spacer(Modifier.height(8.dp))
                    PnsaTextField(query, {
                        query = it
                        graph.prefs.mapQuery = it
                    }, "Nom, service ou commune")
                    if (!navigation.granted) {
                        Spacer(Modifier.height(8.dp))
                        StatusBanner("Autorise la localisation pour suivre ton déplacement.")
                    } else if (navigation.user == null) {
                        Spacer(Modifier.height(8.dp))
                        StatusBanner("Recherche de ta position…")
                    }
                }
                Box(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (followUser) PnsaBlue else Color.White)
                        .quietClick(onClick = { followUser = true }),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Suivre ma position",
                        tint = if (followUser) Color.White else PnsaBlue,
                    )
                }
                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                ) {
                    if (navigation.route != null) {
                        StatusBanner("En route · ${navigation.route.summary}")
                        Spacer(Modifier.height(8.dp))
                    }
                    if (selected != null) {
                        GpsPlaceCard(selected) {
                            selected.id?.let { navigator.push(AppDestination.StructureDetail(it)) }
                        }
                    }
                }
            } else {
                PageBackdrop {
                    LazyColumn(
                        Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text("Annuaire GPS Kinshasa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                if (usesGoogleMaps()) "Google Maps interactif · suivi de ta position."
                                else "Carte locale Kinshasa · centres disponibles hors ligne.",
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PnsaChip(label = "Carte", selected = false, onClick = {
                                    showMap = true
                                    followUser = true
                                    graph.prefs.mapShowMap = true
                                })
                                PnsaChip(label = "Liste", selected = true, onClick = {})
                            }
                            Spacer(Modifier.height(8.dp))
                            PnsaTextField(query, {
                                query = it
                                graph.prefs.mapQuery = it
                            }, "Nom, service ou commune")
                            Spacer(Modifier.height(8.dp))
                            PnsaTextField(city, {
                                city = it
                                graph.prefs.mapCity = it
                            }, "Commune / ville")
                            Spacer(Modifier.height(8.dp))
                            PrimaryAction(if (loading) "Recherche…" else "Actualiser l’annuaire") { load() }
                        }
                        if (error != null && filtered.isEmpty()) {
                            item { ErrorState(error.orEmpty(), onRetry = { load() }) }
                        }
                        items(filtered, key = { it.id ?: it.displayName }) { structure ->
                            PnsaCard(onClick = {
                                selectedId = structure.id
                                graph.prefs.mapSelectedId = structure.id
                                showMap = true
                                followUser = true
                                graph.prefs.mapShowMap = true
                            }) {
                                Text(structure.displayName, fontWeight = FontWeight.SemiBold)
                                Text("${structure.province ?: "—"} · ${structure.displayCity}")
                                Text(structure.displayAddress, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (structure.hasCoordinates) {
                                    Spacer(Modifier.height(6.dp))
                                    MetaRow(listOf(KinshasaMapMath.formatCoord(structure.latitude, structure.longitude)))
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

@Composable
fun StructureDetailScreen(id: Long, navigator: AppNavigator, onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    var item by remember { mutableStateOf<HealthStructure?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        loading = true
        val local = KinshasaCenters.byId(id)
        if (local != null) {
            item = local
            loading = false
            return@LaunchedEffect
        }
        runCatching { graph.catalog.structure(id) }
            .onSuccess {
                item = it
                graph.profile.track("structure_view", mapOf("structure_id" to id.toString()))
            }
            .onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
        loading = false
    }

    PnsaScaffold(topBar = { PnsaTopBar(item?.displayName ?: "Structure", onBack = onBack) }) { padding ->
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
                        val navigation = rememberMapNavigation(structure)
                        Spacer(Modifier.height(8.dp))
                        if (!navigation.granted) {
                            StatusBanner("Autorise la localisation pour tracer l’itinéraire jusqu’à cette structure.")
                            Spacer(Modifier.height(8.dp))
                        } else if (navigation.route != null) {
                            StatusBanner("Itinéraire : ${navigation.route.summary}")
                            Spacer(Modifier.height(8.dp))
                        }
                        PlatformStructureMap(
                            structures = listOf(structure),
                            selectedId = structure.id,
                            onSelect = {},
                            modifier = Modifier.fillMaxWidth().height(280.dp),
                            userLocation = navigation.user,
                            route = navigation.route,
                            followUser = true,
                        )
                        Spacer(Modifier.height(8.dp))
                        StatusBanner("Position : ${KinshasaMapMath.formatCoord(structure.latitude, structure.longitude)}.")
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

    PnsaScaffold(topBar = { PnsaTopBar("Mes orientations", onBack = { navigator.pop() }) }) { padding ->
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

    PnsaScaffold(topBar = { PnsaTopBar("Orientation", onBack = onBack) }) { padding ->
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

    PnsaScaffold(topBar = { PnsaTopBar("Nouvelle orientation", onBack = onBack) }) { padding ->
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

private data class MapNavigationState(
    val granted: Boolean,
    val user: LatLngPoint?,
    val route: RouteTrack?,
)

@Composable
private fun rememberMapNavigation(destination: HealthStructure?): MapNavigationState {
    val graph = LocalAppGraph.current
    val granted = rememberLocationGranted()
    var user by remember { mutableStateOf<LatLngPoint?>(null) }
    var route by remember { mutableStateOf<RouteTrack?>(null) }
    var lastOrigin by remember { mutableStateOf<LatLngPoint?>(null) }
    var lastDestId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(granted) {
        if (!granted) {
            user = null
            route = null
            return@LaunchedEffect
        }
        locationUpdates().collect { user = it }
    }

    LaunchedEffect(granted, user, destination?.id) {
        val origin = user
        val dest = destination
        if (!granted || origin == null || dest == null || !dest.hasCoordinates) {
            if (dest?.id != lastDestId) route = null
            return@LaunchedEffect
        }
        val destPoint = LatLngPoint(dest.latitude!!, dest.longitude!!)
        val sameDest = dest.id == lastDestId
        if (sameDest && route != null && !shouldRefreshRoute(lastOrigin, origin)) {
            return@LaunchedEffect
        }
        lastOrigin = origin
        lastDestId = dest.id
        route = graph.directions.route(origin, destPoint)
    }

    return MapNavigationState(granted = granted, user = user, route = route)
}
