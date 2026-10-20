package app.partners.pnsa

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
private fun PnsaRoot() {
    val navigator = remember { AppNavigator(AppDestination.Structures) }
    CompositionLocalProvider(LocalEmbeddedChrome provides true) {
        PnsaScaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                TikTokBottomBar(
                    selected = navigator.tab,
                    onSelect = { navigator.openTab(it) },
                )
            },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                MainTab.entries.forEach { tab ->
                    KeepAlivePane(visible = navigator.tab == tab) {
                        CompositionLocalProvider(LocalTabActive provides (navigator.tab == tab)) {
                            SignedInFlowForTab(navigator, tab)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SignedInFlowForTab(navigator: AppNavigator, tab: MainTab) {
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
