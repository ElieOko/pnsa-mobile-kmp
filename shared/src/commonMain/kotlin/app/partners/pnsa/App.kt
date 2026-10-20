package app.partners.pnsa

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.partners.pnsa.core.di.AppGraph
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.ui.components.KeepAlivePane
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.TikTokBottomBar
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.navigation.MainTab
import app.partners.pnsa.core.ui.theme.LocalEmbeddedChrome
import app.partners.pnsa.core.ui.theme.LocalTabActive
import app.partners.pnsa.core.ui.theme.PnsaTheme
import app.partners.pnsa.features.auth.ui.LegalScreen
import app.partners.pnsa.features.content.ui.ContentDetailScreen
import app.partners.pnsa.features.content.ui.ContentListScreen
import app.partners.pnsa.features.forum.ui.AdviceChatScreen
import app.partners.pnsa.features.forum.ui.AdviceListScreen
import app.partners.pnsa.features.forum.ui.ForumDetailScreen
import app.partners.pnsa.features.forum.ui.ForumListScreen
import app.partners.pnsa.features.home.ui.HomeScreen
import app.partners.pnsa.features.quiz.ui.QuizListScreen
import app.partners.pnsa.features.quiz.ui.QuizPlayScreen
import app.partners.pnsa.features.structure.ui.OrientationCreateScreen
import app.partners.pnsa.features.structure.ui.OrientationDetailScreen
import app.partners.pnsa.features.structure.ui.OrientationListScreen
import app.partners.pnsa.features.structure.ui.StructureDetailScreen
import app.partners.pnsa.features.structure.ui.StructureListScreen
import app.partners.pnsa.features.user.ui.HelpScreen
import app.partners.pnsa.features.user.ui.ProfileEditScreen
import app.partners.pnsa.features.user.ui.ProfileScreen

@Composable
fun App() {
    val graph = remember { AppGraph() }
    PnsaTheme(darkTheme = false) {
        CompositionLocalProvider(LocalAppGraph provides graph) {
            PnsaRoot(graph)
        }
    }
}

@Composable
private fun PnsaRoot(graph: AppGraph) {
    val navigator = remember {
        AppNavigator(AppDestination.Home).apply {
            openTab(MainTab.Structures)
        }
    }
    val visited = remember { mutableStateListOf(navigator.tab) }
    val inQuizPlay = navigator.current is AppDestination.QuizPlay
    // Aucune topbar : seule la bottom bar blanche (racines d'onglets) sert de chrome.
    val showBottomBar = !inQuizPlay && !navigator.canPop

    LaunchedEffect(navigator.tab) {
        if (navigator.tab !in visited) visited += navigator.tab
    }

    CompositionLocalProvider(LocalEmbeddedChrome provides true) {
        PnsaScaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    TikTokBottomBar(
                        selected = navigator.tab,
                        onSelect = { navigator.openTab(it) },
                    )
                }
            },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                MainTab.entries.forEach { tab ->
                    if (tab in visited) {
                        KeepAlivePane(visible = navigator.tab == tab) {
                            CompositionLocalProvider(LocalTabActive provides (navigator.tab == tab)) {
                                SignedInFlow(navigator, tab)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * La racine de chaque onglet reste toujours composée : quand un détail est poussé,
 * il s'affiche par-dessus (fond blanc) sans détruire l'écran racine — l'état de la
 * carte (vue, position, sélection) est donc intégralement conservé au retour.
 */
@Composable
private fun SignedInFlow(navigator: AppNavigator, tab: MainTab) {
    val dest = navigator.currentFor(tab)
    Box(Modifier.fillMaxSize()) {
        TabRootScreen(navigator, tab)
        if (navigator.depthFor(tab) > 1) {
            Surface(Modifier.fillMaxSize(), color = Color.White) {
                DetailScreen(navigator, dest)
            }
        }
    }
}

@Composable
private fun TabRootScreen(navigator: AppNavigator, tab: MainTab) {
    when (tab) {
        MainTab.Home -> HomeScreen(navigator)
        MainTab.Learn -> ContentListScreen(navigator)
        MainTab.Quiz -> QuizListScreen(navigator)
        MainTab.Structures -> StructureListScreen(navigator)
        MainTab.Forum -> ForumListScreen(navigator)
        MainTab.Profile -> ProfileScreen(navigator)
    }
}

@Composable
private fun DetailScreen(navigator: AppNavigator, dest: AppDestination) {
    when (dest) {
        is AppDestination.ContentDetail -> ContentDetailScreen(dest.id, onBack = { navigator.pop() })
        is AppDestination.QuizPlay -> QuizPlayScreen(dest.id, onBack = { navigator.pop() })
        is AppDestination.StructureDetail -> StructureDetailScreen(dest.id, navigator, onBack = { navigator.pop() })
        AppDestination.Orientations -> OrientationListScreen(navigator)
        is AppDestination.OrientationDetail -> OrientationDetailScreen(dest.id, onBack = { navigator.pop() })
        is AppDestination.OrientationCreate -> OrientationCreateScreen(dest.structureId, navigator, onBack = { navigator.pop() })
        is AppDestination.ForumDetail -> ForumDetailScreen(dest.id, onBack = { navigator.pop() })
        AppDestination.Advice -> AdviceListScreen(navigator)
        is AppDestination.AdviceChat -> AdviceChatScreen(dest.id, onBack = { navigator.pop() })
        AppDestination.ProfileEdit -> ProfileEditScreen(onBack = { navigator.pop() })
        AppDestination.Help, AppDestination.PublicHelp -> HelpScreen(onBack = { navigator.pop() })
        AppDestination.PublicLegal -> LegalScreen(onBack = { navigator.pop() })
        AppDestination.Profile, AppDestination.More -> ProfileScreen(navigator)
        else -> TabRootScreen(navigator, navigator.tab)
    }
}
