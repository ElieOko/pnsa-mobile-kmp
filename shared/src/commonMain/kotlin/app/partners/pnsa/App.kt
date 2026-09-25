package app.partners.pnsa

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import app.partners.pnsa.core.di.AppGraph
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.ui.components.KeepAlivePane
import app.partners.pnsa.core.ui.components.PnsaDrawerContent
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.TikTokBottomBar
import app.partners.pnsa.core.ui.components.TikTokTopBar
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.navigation.MainTab
import app.partners.pnsa.core.ui.theme.LocalEmbeddedChrome
import app.partners.pnsa.core.ui.theme.PnsaTheme
import app.partners.pnsa.features.auth.ui.LegalScreen
import app.partners.pnsa.features.auth.ui.LoginScreen
import app.partners.pnsa.features.auth.ui.RegisterScreen
import app.partners.pnsa.features.auth.ui.WelcomeScreen
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
import kotlinx.coroutines.launch

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
    val token by graph.session.token.collectAsState()
    val signedIn = !token.isNullOrBlank()
    val navigator = remember(signedIn) {
        AppNavigator(if (signedIn) AppDestination.Home else AppDestination.Welcome)
    }

    if (!signedIn) {
        AuthFlow(navigator)
        return
    }

    val user by graph.session.user.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val visited = remember { mutableStateListOf(MainTab.Home) }
    val showChrome = navigator.current !is AppDestination.QuizPlay

    LaunchedEffect(navigator.tab) {
        if (navigator.tab !in visited) visited += navigator.tab
    }

    fun logout() {
        scope.launch {
            drawerState.close()
            graph.auth.logout()
            graph.cache.clear()
            graph.screens.clear()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !navigator.canPop,
        drawerContent = {
            PnsaDrawerContent(
                name = user?.displayName ?: "Professionnel PNSA",
                email = user?.email.orEmpty(),
                initials = user?.initials ?: "P",
                onDestination = { dest ->
                    scope.launch { drawerState.close() }
                    navigator.push(dest)
                },
                onLogout = { logout() },
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
                    MainTab.entries.forEach { tab ->
                        if (tab in visited) {
                            KeepAlivePane(visible = navigator.tab == tab) {
                                SignedInFlow(navigator, tab) { logout() }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthFlow(navigator: AppNavigator) {
    when (val dest = navigator.current) {
            AppDestination.Welcome -> WelcomeScreen(
                onLogin = { navigator.push(AppDestination.Login) },
                onRegister = { navigator.push(AppDestination.Register) },
                onHelp = { navigator.push(AppDestination.PublicHelp) },
                onLegal = { navigator.push(AppDestination.PublicLegal) },
            )
            AppDestination.Login -> LoginScreen(
                onBack = { navigator.pop() },
                onLoggedIn = { navigator.replace(AppDestination.Home) },
                onRegister = { navigator.replaceCurrent(AppDestination.Register) },
                onHelp = { navigator.push(AppDestination.PublicHelp) },
            )
            AppDestination.Register -> RegisterScreen(
                onBack = { navigator.pop() },
                onRegistered = { navigator.replace(AppDestination.Home) },
                onLegal = { navigator.push(AppDestination.PublicLegal) },
            )
            AppDestination.PublicHelp -> HelpScreen(onBack = { navigator.pop() })
            AppDestination.PublicLegal -> LegalScreen(onBack = { navigator.pop() })
            else -> WelcomeScreen(
                onLogin = { navigator.push(AppDestination.Login) },
                onRegister = { navigator.push(AppDestination.Register) },
                onHelp = { navigator.push(AppDestination.PublicHelp) },
                onLegal = { navigator.push(AppDestination.PublicLegal) },
            )
        }
}

@Composable
private fun SignedInFlow(navigator: AppNavigator, tab: MainTab, onLoggedOut: () -> Unit) {
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
        AppDestination.Profile -> ProfileScreen(navigator, onLoggedOut)
        AppDestination.ProfileEdit -> ProfileEditScreen(onBack = { navigator.pop() })
        AppDestination.Help, AppDestination.PublicHelp -> HelpScreen(onBack = { navigator.pop() })
        AppDestination.PublicLegal -> LegalScreen(onBack = { navigator.pop() })
        AppDestination.More -> ProfileScreen(navigator, onLoggedOut)
        AppDestination.Welcome, AppDestination.Login, AppDestination.Register -> HomeScreen(navigator)
    }
}
