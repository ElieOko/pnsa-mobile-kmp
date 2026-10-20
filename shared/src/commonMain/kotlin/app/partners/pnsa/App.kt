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
            PnsaRoot()
        }
    }
}

@Composable
private fun PnsaRoot() {
    val navigator = remember {
        AppNavigator(AppDestination.Home).apply {
            openTab(MainTab.Structures)
        }
    }

    val visited = remember { mutableStateListOf(navigator.tab) }
    val showChrome = navigator.current !is AppDestination.QuizPlay

    LaunchedEffect(navigator.tab) {
        if (navigator.tab !in visited) visited += navigator.tab
    }

    CompositionLocalProvider(LocalEmbeddedChrome provides true) {
        PnsaScaffold(
            containerColor = Color.White,
            bottomBar = {
                if (showChrome) {
                    TikTokBottomBar(
                        selected = navigator.tab,
                        onSelect = { navigator.openTab(it) },
                    )
                }
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
        AppDestination.Home -> HomeScreen(navigator)
        AppDestination.Learn -> ContentListScreen(navigator)
        is AppDestination.ContentDetail -> ContentDetailScreen(dest.id, onBack = { navigator.pop() })
        AppDestination.Quizzes -> QuizListScreen(navigator)
        is AppDestination.QuizPlay -> QuizPlayScreen(dest.id, onBack = { navigator.pop() })
        AppDestination.Structures -> StructureListScreen(navigator)
        is AppDestination.StructureDetail -> StructureDetailScreen(dest.id, navigator, onBack = { navigator.pop() })
        AppDestination.Orientations -> OrientationListScreen(navigator)
        is AppDestination.OrientationDetail -> OrientationDetailScreen(dest.id, onBack = { navigator.pop() })
        is AppDestination.OrientationCreate -> OrientationCreateScreen(dest.structureId, navigator, onBack = { navigator.pop() })
        AppDestination.Forum -> ForumListScreen(navigator)
        is AppDestination.ForumDetail -> ForumDetailScreen(dest.id, onBack = { navigator.pop() })
        AppDestination.Advice -> AdviceListScreen(navigator)
        is AppDestination.AdviceChat -> AdviceChatScreen(dest.id, onBack = { navigator.pop() })
        AppDestination.Profile -> ProfileScreen(navigator)
        AppDestination.ProfileEdit -> ProfileEditScreen(onBack = { navigator.pop() })
        AppDestination.Help, AppDestination.PublicHelp -> HelpScreen(onBack = { navigator.pop() })
        AppDestination.PublicLegal -> LegalScreen(onBack = { navigator.pop() })
        AppDestination.More -> ProfileScreen(navigator)
        AppDestination.Welcome, AppDestination.Login, AppDestination.Register -> HomeScreen(navigator)
    }
}
