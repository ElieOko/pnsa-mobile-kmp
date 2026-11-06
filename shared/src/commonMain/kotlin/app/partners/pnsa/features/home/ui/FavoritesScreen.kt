package app.partners.pnsa.features.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.theme.RepereInk
import app.partners.pnsa.core.ui.theme.RepereMuted
import app.partners.pnsa.features.structure.data.KinshasaCenters
import app.partners.pnsa.features.structure.ui.StructureVisualCard
import app.partners.pnsa.features.structure.ui.prefetchStructurePhotos

@Composable
fun FavoritesScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    val favoriteIds = graph.favorites.ids
    val structures = remember(graph.screens.structures, favoriteIds) {
        KinshasaCenters.mergeWith(graph.screens.structures).filter { it.id in favoriteIds }
    }
    LaunchedEffect(structures) {
        prefetchStructurePhotos(structures.mapNotNull { it.photoUrl })
    }

    PnsaScaffold { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Favoris", color = RepereInk, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                Text(
                    "Les structures que tu gardes sous la main.",
                    color = RepereMuted,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(6.dp))
            }
            if (structures.isEmpty()) {
                item {
                    EmptyState(
                        "Aucun favori pour le moment",
                        "Ouvre une fiche et appuie sur le cœur pour la retrouver ici.",
                    )
                }
            } else {
                items(structures, key = { it.id ?: it.displayName }) { structure ->
                    StructureVisualCard(structure) {
                        structure.id?.let { navigator.push(AppDestination.StructureDetail(it)) }
                    }
                }
            }
            item { Spacer(Modifier.height(18.dp)) }
        }
    }
}
