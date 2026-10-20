package app.partners.pnsa

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.partners.pnsa.core.di.AppGraph
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.ui.components.PnsaDrawerContent
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.TikTokBottomBar
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.navigation.MainTab
import app.partners.pnsa.core.ui.theme.LocalEmbeddedChrome
import app.partners.pnsa.core.ui.theme.PnsaTheme
import app.partners.pnsa.features.auth.ui.LegalScreen
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

@Composable
fun App() {
    val graph = remember { AppGraph() }
    PnsaTheme(darkTheme = false) {
        CompositionLocalProvider(LocalAppGraph provides graph) {
            PnsaRoot()
        }
    }
}

@Composable
private fun PnsaRoot(graph: AppGraph) {
    val navigator = remember { AppNavigator(AppDestination.Structures) }

    val user by graph.session.user.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val showChrome = true

    val onMapTab = navigator.tab == MainTab.Structures
    LaunchedEffect(onMapTab) {
        if (onMapTab && drawerState.isOpen) drawerState.close()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !navigator.canPop && !onMapTab,
        drawerContent = {
            PnsaDrawerContent(
                name = user?.displayName ?: "Professionnel PNSA",
                email = user?.email.orEmpty(),
                initials = user?.initials ?: "P",
                onDestination = { dest ->
                    scope.launch { drawerState.close() }
                    navigator.push(dest)
                },
                showLogout = false,
                onLogout = {},
            )
        },
    ) {
        CompositionLocalProvider(LocalEmbeddedChrome provides true) {
            PnsaScaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    if (showChrome) {
                        TikTokTopBar(
                            title = navigator.title(),
                            canPop = navigator.canPop,
                            onMenu = { scope.launch { drawerState.open() } },
                            onBack = { navigator.pop() },
                            initials = user?.initials ?: "P",
                            onAvatar = { navigator.push(AppDestination.Profile) },
                        )
                    }
                },
                bottomBar = {
                    if (showChrome) {
                        TikTokBottomBar(
                            selected = navigator.tab,
                            onSelect = { navigator.openTab(it) },
                        )
                    }
                },
            ) { padding ->
                Box(Modifier.padding(padding).fillMaxSize()) {
                    SignedInFlow(navigator, navigator.tab)
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
        AppDestination.Orientations -> OrientationListScreen(navigator)
        is AppDestination.OrientationDetail -> OrientationDetailScreen(dest.id, onBack = { navigator.pop() })
        is AppDestination.OrientationCreate -> OrientationCreateScreen(dest.structureId, navigator, onBack = { navigator.pop() })
        AppDestination.Profile -> ProfileScreen(navigator)
        AppDestination.ProfileEdit -> ProfileEditScreen(onBack = { navigator.pop() })
        AppDestination.Help, AppDestination.PublicHelp -> HelpScreen(onBack = { navigator.pop() })
        AppDestination.PublicLegal -> LegalScreen(onBack = { navigator.pop() })
        AppDestination.More -> ProfileScreen(navigator)
        AppDestination.Home,
        AppDestination.Learn,
        is AppDestination.ContentDetail,
        AppDestination.Quizzes,
        is AppDestination.QuizPlay,
        AppDestination.Forum,
        is AppDestination.ForumDetail,
        AppDestination.Advice,
        is AppDestination.AdviceChat,
        AppDestination.Welcome,
        AppDestination.Login,
        AppDestination.Register,
        -> StructureListScreen(navigator)
    }
}
