package app.partners.pnsa.features.structure.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import app.partners.pnsa.core.ui.components.InlineBackHeader
import app.partners.pnsa.core.ui.components.KeepAlivePane
import app.partners.pnsa.core.ui.components.KinshasaMapMath
import app.partners.pnsa.core.ui.components.LoadingState
import app.partners.pnsa.core.ui.components.MetaRow
import app.partners.pnsa.core.ui.components.PageBackdrop
import app.partners.pnsa.core.ui.components.PnsaCard
import app.partners.pnsa.core.ui.components.PnsaChip
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.PnsaTextField
import app.partners.pnsa.core.ui.components.PnsaTopBar
import app.partners.pnsa.core.ui.components.PrimaryAction
import app.partners.pnsa.core.ui.components.SecondaryAction
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.components.quietClick
import app.partners.pnsa.core.ui.map.MapViewStyle
import app.partners.pnsa.core.ui.map.PlatformStructureMap
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.theme.PnsaBlue
import app.partners.pnsa.core.ui.theme.PnsaInk
import app.partners.pnsa.core.ui.theme.PnsaMuted
import app.partners.pnsa.core.ui.theme.PnsaPink
import app.partners.pnsa.core.ui.theme.PnsaPinkSoft
import app.partners.pnsa.core.ui.theme.bagdeColor
import app.partners.pnsa.core.ui.theme.wsColor
import app.partners.pnsa.core.util.formatIsoDate
import app.partners.pnsa.features.structure.data.KinshasaCenters
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import app.partners.pnsa.features.structure.domain.models.Orientation
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val INITIAL_DIRECTORY_LIMIT = 15
private const val DIRECTORY_PAGE_STEP = 10

@Composable
fun StructureListScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf(graph.screens.structures.ifEmpty { KinshasaCenters.all }) }
    var province by remember { mutableStateOf("") }
    var city by remember { mutableStateOf(graph.prefs.mapCity) }
    var query by remember { mutableStateOf(graph.prefs.mapQuery) }
    var loading by remember { mutableStateOf(!graph.screens.structuresLoaded) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedId by remember { mutableStateOf(graph.prefs.mapSelectedId ?: items.firstOrNull()?.id) }
    var showMap by remember { mutableStateOf(graph.prefs.mapShowMap) }
    var mapOnlyMode by remember { mutableStateOf(graph.prefs.mapOnlyMode) }
    var mapStyle by remember {
        val saved = graph.prefs.mapStyle
        mutableStateOf(
            MapViewStyle.entries.firstOrNull { it.name == saved && it != MapViewStyle.Terrain }
                ?: MapViewStyle.Standard,
        )
    }
    var visibleCount by remember { mutableIntStateOf(INITIAL_DIRECTORY_LIMIT) }
    var selectedServiceFilter by remember { mutableStateOf(graph.prefs.mapServiceFilter) }
    var selectedTypeFilter by remember { mutableStateOf(graph.prefs.mapTypeFilter) }
    val directoryListState = rememberLazyListState()

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

    val serviceFilters = remember(items) {
        val distinctServices = items
            .asSequence()
            .mapNotNull { it.services }
            .flatMap { it.split(".", ",", ";") }
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.equals("Aucun service renseigné", ignoreCase = true) }
            .distinct()
            .take(12)
            .toList()
        listOf("Tous") + distinctServices
    }

    val typeFilters = remember(items) {
        val distinctTypes = items.mapNotNull { it.structureType?.trim()?.takeIf { s -> s.isNotEmpty() } }.distinct().take(6)
        listOf("Toutes") + distinctTypes
    }

    val filtered = remember(items, query, city, province, selectedServiceFilter, selectedTypeFilter) {
        items.filter { item ->
            val haystack = listOf(item.displayName, item.displayCity, item.displayAddress, item.services, item.structureType)
                .joinToString(" ")
                .lowercase()
            val matchesQuery = query.isBlank() || haystack.contains(query.lowercase())
            val matchesCity = city.isBlank() || item.displayCity.contains(city, ignoreCase = true)
            val matchesProvince = province.isBlank() || item.province.orEmpty().contains(province, ignoreCase = true)
            val matchesService = selectedServiceFilter == "Tous" ||
                item.services.orEmpty().contains(selectedServiceFilter, ignoreCase = true) ||
                item.structureType.orEmpty().contains(selectedServiceFilter, ignoreCase = true)
            val matchesType = selectedTypeFilter == "Toutes" || item.structureType.equals(selectedTypeFilter, ignoreCase = true)
            matchesQuery && matchesCity && matchesProvince && matchesService && matchesType
        }
    }
    val displayedDirectoryItems = remember(filtered, visibleCount) {
        filtered.take(visibleCount)
    }

    // Progressive bidirectional scroll on Structures SSR directory
    LaunchedEffect(directoryListState, filtered.size) {
        snapshotFlow {
            val layoutInfo = directoryListState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val firstVisible = directoryListState.firstVisibleItemIndex
            firstVisible to lastVisible
        }
            .distinctUntilChanged()
            .collect { (firstVisible, lastVisible) ->
                if (lastVisible >= visibleCount - 2 && visibleCount < filtered.size) {
                    visibleCount = (visibleCount + DIRECTORY_PAGE_STEP).coerceAtMost(filtered.size)
                } else if (firstVisible == 0 && visibleCount > INITIAL_DIRECTORY_LIMIT) {
                    visibleCount = INITIAL_DIRECTORY_LIMIT
                } else if (lastVisible + DIRECTORY_PAGE_STEP + 3 < visibleCount && visibleCount > INITIAL_DIRECTORY_LIMIT) {
                    visibleCount = (lastVisible + DIRECTORY_PAGE_STEP).coerceAtLeast(INITIAL_DIRECTORY_LIMIT)
                }
            }
    }

    val selected = filtered.firstOrNull { it.id == selectedId } ?: items.firstOrNull { it.id == selectedId }
    val navigation = rememberMapNavigation(selected)
    var followUser by remember { mutableStateOf(true) }

    PnsaScaffold { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            // Map pane kept alive so camera/state/selection are never lost when switching views
            KeepAlivePane(visible = showMap) {
                Box(Modifier.fillMaxSize()) {
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
                        mapStyle = mapStyle,
                    )

                    // Top overlay: 1) Carte & Annuaire at the very top, 2) Services directly underneath
                    AnimatedVisibility(
                        visible = showMap && !mapOnlyMode,
                        enter = fadeIn(tween(260)) + slideInVertically(tween(260)) { -it / 2 },
                        exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { -it / 2 },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 8.dp, end = 62.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = Color.White,
                                shadowElevation = 5.dp,
                                border = BorderStroke(1.dp, bagdeColor.copy(alpha = 0.25f)),
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    PnsaChip(
                                        label = "Carte",
                                        selected = true,
                                        onClick = {},
                                    )
                                    PnsaChip(
                                        label = "Annuaire (${filtered.size})",
                                        selected = false,
                                        onClick = {
                                            showMap = false
                                            graph.prefs.mapShowMap = false
                                        },
                                    )
                                }
                            }

                            // Services filter chips directly below Carte & Annuaire
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                serviceFilters.forEach { serviceLabel ->
                                    PnsaChip(
                                        label = serviceLabel,
                                        selected = selectedServiceFilter == serviceLabel,
                                        onClick = {
                                            selectedServiceFilter = serviceLabel
                                            graph.prefs.mapServiceFilter = serviceLabel
                                        },
                                    )
                                }
                            }
                        }
                    }

                    // Vertical icon-only controls at top-right (NO card wrapper): Standard, Satellite, and Map-only toggle
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val isStandard = mapStyle == MapViewStyle.Standard
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isStandard) PnsaPink else Color.White.copy(alpha = 0.92f))
                                .quietClick {
                                    mapStyle = MapViewStyle.Standard
                                    graph.prefs.mapStyle = MapViewStyle.Standard.name
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Vue Standard",
                                tint = if (isStandard) Color.White else PnsaPink,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        val isSatellite = mapStyle == MapViewStyle.Satellite
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isSatellite) PnsaPink else Color.White.copy(alpha = 0.92f))
                                .quietClick {
                                    mapStyle = MapViewStyle.Satellite
                                    graph.prefs.mapStyle = MapViewStyle.Satellite.name
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Vue Satellite",
                                tint = if (isSatellite) Color.White else PnsaPink,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        // Toggle icon to view the map by itself without cards/overlays
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (mapOnlyMode) PnsaPink else Color.White.copy(alpha = 0.92f))
                                .quietClick {
                                    mapOnlyMode = !mapOnlyMode
                                    graph.prefs.mapOnlyMode = mapOnlyMode
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (mapOnlyMode) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (mapOnlyMode) "Afficher les cartes" else "Voir uniquement la carte",
                                tint = if (mapOnlyMode) Color.White else PnsaPink,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    // MyLocation floating button (pink icon)
                    if (!mapOnlyMode) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                                .size(42.dp)
                                .quietClick(onClick = { followUser = true }),
                            shape = CircleShape,
                            color = if (followUser) PnsaPink else Color.White,
                            shadowElevation = 5.dp,
                            border = BorderStroke(1.dp, PnsaPink),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = "Suivre ma position",
                                    tint = if (followUser) Color.White else PnsaPink,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }

                    // Animated compact bottom selected structure card (hidden in map-only mode)
                    Column(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        AnimatedVisibility(
                            visible = !mapOnlyMode && navigation.route != null,
                            enter = fadeIn() + slideInVertically { it },
                            exit = fadeOut() + slideOutVertically { it },
                        ) {
                            Column {
                                StatusBanner("En route · ${navigation.route?.summary.orEmpty()}")
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                        AnimatedVisibility(
                            visible = !mapOnlyMode && selected != null,
                            enter = fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 2 },
                            exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { it / 2 },
                        ) {
                            if (selected != null) {
                                GpsPlaceCard(
                                    item = selected,
                                    onChat = selected.id?.let { id -> { navigator.push(AppDestination.StructureChat(id)) } },
                                ) {
                                    selected.id?.let { navigator.push(AppDestination.StructureDetail(it)) }
                                }
                            }
                        }
                    }
                }
            }

            // Directory list pane (progressive scroll: loads on scroll down, hides items on scroll up)
            KeepAlivePane(visible = !showMap) {
                PageBackdrop {
                    LazyColumn(
                        state = directoryListState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = Color.White,
                                shadowElevation = 3.dp,
                                border = BorderStroke(1.dp, bagdeColor.copy(alpha = 0.25f)),
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    PnsaChip(
                                        label = "Carte",
                                        selected = false,
                                        onClick = {
                                            showMap = true
                                            graph.prefs.mapShowMap = true
                                        },
                                    )
                                    PnsaChip(
                                        label = "Annuaire (${filtered.size})",
                                        selected = true,
                                        onClick = {},
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                serviceFilters.forEach { serviceLabel ->
                                    PnsaChip(
                                        label = serviceLabel,
                                        selected = selectedServiceFilter == serviceLabel,
                                        onClick = {
                                            selectedServiceFilter = serviceLabel
                                            graph.prefs.mapServiceFilter = serviceLabel
                                            visibleCount = INITIAL_DIRECTORY_LIMIT
                                        },
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            InlineBackHeader(
                                title = "Structures SSR",
                                subtitle = "${displayedDirectoryItems.size} affichées sur ${filtered.size} structures disponibles",
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                typeFilters.forEach { filterLabel ->
                                    PnsaChip(
                                        label = filterLabel,
                                        selected = selectedTypeFilter == filterLabel,
                                        onClick = {
                                            selectedTypeFilter = filterLabel
                                            graph.prefs.mapTypeFilter = filterLabel
                                            visibleCount = INITIAL_DIRECTORY_LIMIT
                                        },
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            PnsaTextField(
                                value = query,
                                onValueChange = {
                                    query = it
                                    graph.prefs.mapQuery = it
                                    visibleCount = INITIAL_DIRECTORY_LIMIT
                                },
                                label = "Filtrer par nom, service ou commune",
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.weight(1f)) {
                                    PnsaTextField(
                                        value = city,
                                        onValueChange = {
                                            city = it
                                            graph.prefs.mapCity = it
                                            visibleCount = INITIAL_DIRECTORY_LIMIT
                                        },
                                        label = "Commune / ville",
                                    )
                                }
                            }
                        }
                        if (error != null && filtered.isEmpty()) {
                            item { ErrorState(error.orEmpty(), onRetry = { load() }) }
                        }
                        itemsIndexed(
                            items = displayedDirectoryItems,
                            key = { _, it -> it.id ?: it.displayName },
                        ) { _, structure ->
                            PnsaCard(
                                onClick = {
                                    selectedId = structure.id
                                    graph.prefs.mapSelectedId = structure.id
                                    showMap = true
                                    followUser = false
                                    graph.prefs.mapShowMap = true
                                },
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(PnsaPinkSoft),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.Default.Place, contentDescription = null, tint = PnsaPink)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(structure.displayName, fontWeight = FontWeight.Bold, color = PnsaInk)
                                        Text(
                                            "${structure.province ?: "Kinshasa"} · ${structure.displayCity}",
                                            color = bagdeColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(structure.displayAddress, color = PnsaMuted, fontSize = 13.sp)
                                    }
                                }
                                if (structure.hasCoordinates) {
                                    Spacer(Modifier.height(8.dp))
                                    MetaRow(
                                        listOfNotNull(
                                            structure.structureType,
                                            KinshasaMapMath.formatCoord(structure.latitude, structure.longitude),
                                        ),
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(Modifier.height(8.dp))
                            PrimaryAction(if (loading) "Actualisation…" else "Actualiser l’annuaire") { load() }
                            Spacer(Modifier.height(24.dp))
                        }
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
    var detailMapStyle by remember { mutableStateOf(MapViewStyle.Standard) }

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

    PnsaScaffold { padding ->
        when {
            loading -> LoadingState()
            error != null -> ErrorState(error.orEmpty())
            item == null -> EmptyState("Fiche introuvable", "Cette structure n’est plus proposée.")
            else -> {
                val structure = item!!
                Column(
                    Modifier
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    InlineBackHeader(
                        title = structure.displayName,
                        subtitle = "${structure.displayCity} · ${structure.structureType ?: "Centre SSR"}",
                        onBack = onBack,
                    )
                    Spacer(Modifier.height(8.dp))
                    MetaRow(listOfNotNull(structure.structureType, structure.province, structure.displayCity))
                    Spacer(Modifier.height(12.dp))
                    PnsaCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = bagdeColor, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(structure.displayAddress, fontWeight = FontWeight.Medium, color = PnsaInk)
                        }
                        if (!structure.phone.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = bagdeColor, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Tél. ${structure.phone}", color = PnsaInk)
                            }
                        }
                        if (!structure.email.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = bagdeColor, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(structure.email.orEmpty(), color = PnsaInk)
                            }
                        }
                        if (!structure.openingHours.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = bagdeColor, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Horaires : ${structure.openingHours}", color = PnsaMuted)
                            }
                        }
                        if (!structure.services.isNullOrBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Text("Services disponibles", fontWeight = FontWeight.Bold, color = bagdeColor)
                            Spacer(Modifier.height(4.dp))
                            Text(structure.services.orEmpty(), color = PnsaInk)
                        }
                    }
                    if (structure.hasCoordinates) {
                        val navigation = rememberMapNavigation(structure)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MapViewStyle.entries.forEach { style ->
                                PnsaChip(
                                    label = style.label,
                                    selected = detailMapStyle == style,
                                    onClick = { detailMapStyle = style },
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        if (navigation.route != null) {
                            StatusBanner("Itinéraire : ${navigation.route.summary}")
                            Spacer(Modifier.height(8.dp))
                        }
                        PlatformStructureMap(
                            structures = listOf(structure),
                            selectedId = structure.id,
                            onSelect = {},
                            modifier = Modifier.fillMaxWidth().height(260.dp),
                            userLocation = navigation.user,
                            route = navigation.route,
                            followUser = true,
                            mapStyle = detailMapStyle,
                        )
                        Spacer(Modifier.height(8.dp))
                        StatusBanner("Position : ${KinshasaMapMath.formatCoord(structure.latitude, structure.longitude)}.")
                    } else {
                        Spacer(Modifier.height(8.dp))
                        StatusBanner("Coordonnées GPS non renseignées. La fiche reste consultable.")
                    }
                    Spacer(Modifier.height(20.dp))
                    SecondaryAction("Discuter avec cette structure", enabled = structure.id != null) {
                        structure.id?.let { navigator.push(AppDestination.StructureChat(it)) }
                    }
                    Spacer(Modifier.height(8.dp))
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

    PnsaScaffold { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            InlineBackHeader(
                title = "Mes orientations",
                subtitle = "Suivi de tes références vers les structures SSR",
                onBack = { navigator.pop() },
            )
            when {
                loading -> LoadingState()
                error != null -> ErrorState(error.orEmpty(), onRetry = { load() })
                items.isEmpty() -> EmptyState(
                    "Aucune orientation",
                    "Choisis une structure dans l’annuaire pour créer une référence. Une intention de visite n’est pas un service reçu.",
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(items, key = { _, it -> it.id ?: it.clientRequestId.orEmpty() }) { _, item ->
                        PnsaCard(onClick = { item.id?.let { navigator.push(AppDestination.OrientationDetail(it)) } }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(item.healthStructure?.displayName ?: "Structure", fontWeight = FontWeight.Bold, color = PnsaInk)
                                Surface(shape = CircleShape, color = wsColor) {
                                    Text(
                                        item.statusLabel,
                                        color = bagdeColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(item.reason ?: "Sans motif précisé", color = PnsaMuted)
                        }
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

    PnsaScaffold { padding ->
        when {
            loading -> LoadingState()
            error != null -> ErrorState(error.orEmpty())
            item == null -> EmptyState("Introuvable", "Cette référence n’est plus accessible.")
            else -> {
                val orientation = item!!
                Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
                    InlineBackHeader(
                        title = orientation.healthStructure?.displayName ?: "Orientation",
                        subtitle = "État : ${orientation.statusLabel} · ${formatIsoDate(orientation.createdAt)}",
                        onBack = onBack,
                    )
                    Spacer(Modifier.height(12.dp))
                    PnsaCard {
                        Text("Motif", fontWeight = FontWeight.Bold, color = bagdeColor)
                        Spacer(Modifier.height(4.dp))
                        Text(orientation.reason ?: "Aucun motif saisi.", color = PnsaInk)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Historique", fontWeight = FontWeight.Bold, color = PnsaInk)
                    Spacer(Modifier.height(8.dp))
                    if (orientation.events.isEmpty()) {
                        Text("Aucun événement complémentaire pour le moment.", color = PnsaMuted)
                    } else {
                        orientation.events.forEach { event ->
                            PnsaCard(Modifier.padding(vertical = 6.dp)) {
                                Text(event.status ?: "Événement", fontWeight = FontWeight.Bold, color = bagdeColor)
                                Text(event.comment ?: "", color = PnsaInk)
                                Text(formatIsoDate(event.createdAt), style = MaterialTheme.typography.labelSmall, color = PnsaMuted)
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

    PnsaScaffold { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            InlineBackHeader(
                title = "Nouvelle orientation",
                subtitle = "Demande de référence vers la structure",
                onBack = onBack,
            )
            Spacer(Modifier.height(8.dp))
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
