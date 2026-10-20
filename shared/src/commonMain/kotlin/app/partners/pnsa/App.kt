package app.partners.pnsa

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.partners.pnsa.core.di.AppGraph
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.ui.components.KeepAlivePane
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.TikTokBottomBar
import app.partners.pnsa.core.ui.components.TikTokTopBar
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
import app.partners.pnsa.features.structure.ui.OrientationCreateScreen
import app.partners.pnsa.features.structure.ui.OrientationDetailScreen
import app.partners.pnsa.features.structure.ui.OrientationListScreen
import app.partners.pnsa.features.structure.ui.StructureChatScreen
import app.partners.pnsa.features.structure.ui.StructureConversationsScreen
import app.partners.pnsa.features.structure.ui.StructureDetailScreen
import app.partners.pnsa.features.structure.ui.StructureListScreen
import app.partners.pnsa.features.user.ui.HelpScreen
import app.partners.pnsa.features.user.ui.ProfileEditScreen
import app.partners.pnsa.features.user.ui.ProfileScreen
import kotlinx.coroutines.launch

@Composable
fun App() {
    val graph = remember { AppGraph() }
    PnsaTheme(darkTheme = false) {
        CompositionLocalProvider(LocalAppGraph provides graph) {
            PnsaRoot(
                graph = TODO()
            )
        }
    }
}

@Composable
private fun PnsaRoot() {
    val graph = LocalAppGraph.current
    val navigator = remember {
        var navRef: AppNavigator? = null
        AppNavigator(
            start = AppDestination.Structures,
            onStateChanged = { navRef?.saveTo(graph.prefs) },
        ).also { nav ->
            navRef = nav
            nav.restoreFrom(graph.prefs)
        }
    }
    val visited = remember { mutableStateListOf(navigator.tab) }

    LaunchedEffect(navigator.tab) {
        if (navigator.tab !in visited) visited += navigator.tab
        navigator.saveTo(graph.prefs)
    }

    CompositionLocalProvider(LocalEmbeddedChrome provides true) {
        PnsaScaffold(
            containerColor = Color.White,
            bottomBar = {
                TikTokBottomBar(
                    selected = navigator.tab,
                    onSelect = { navigator.openTab(it) },
                )
            },
        ) { padding ->
            Box(
                Modifier
                    .padding(padding)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .fillMaxSize(),
            ) {
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

@Composable
private fun SignedInFlow(navigator: AppNavigator, tab: MainTab) {
    when (val dest = navigator.currentFor(tab)) {
        AppDestination.Structures -> StructureListScreen(navigator)
        is AppDestination.StructureDetail -> StructureDetailScreen(dest.id, navigator, onBack = { navigator.pop() })
        AppDestination.Messages -> StructureConversationsScreen(navigator)
        is AppDestination.StructureChat -> StructureChatScreen(dest.structureId, onBack = { navigator.pop() })
        AppDestination.Advice -> AdviceListScreen(navigator)
        is AppDestination.AdviceChat -> AdviceChatScreen(dest.id, onBack = { navigator.pop() })
        AppDestination.Orientations -> OrientationListScreen(navigator)
        is AppDestination.OrientationDetail -> OrientationDetailScreen(dest.id, onBack = { navigator.pop() })
        is AppDestination.OrientationCreate -> OrientationCreateScreen(dest.structureId, navigator, onBack = { navigator.pop() })
        AppDestination.Learn -> ContentListScreen(navigator)
        is AppDestination.ContentDetail -> ContentDetailScreen(dest.id, onBack = { navigator.pop() })
        AppDestination.Profile, AppDestination.More -> ProfileScreen(navigator)
        AppDestination.ProfileEdit -> ProfileEditScreen(onBack = { navigator.pop() })
        AppDestination.Help, AppDestination.PublicHelp -> HelpScreen(onBack = { navigator.pop() })
        AppDestination.PublicLegal -> LegalScreen(onBack = { navigator.pop() })
        AppDestination.Home,
        AppDestination.Welcome,
        AppDestination.Login,
        AppDestination.Register,
        -> StructureListScreen(navigator)
    }
}
