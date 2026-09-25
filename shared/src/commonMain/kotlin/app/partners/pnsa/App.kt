package app.partners.pnsa

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import app.partners.pnsa.core.di.AppGraph
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.navigation.MainTab
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
import app.partners.pnsa.features.user.ui.MoreMenuScreen
import app.partners.pnsa.features.user.ui.ProfileEditScreen
import app.partners.pnsa.features.user.ui.ProfileScreen

@Composable
fun App() {
    val graph = remember { AppGraph() }
    PnsaTheme {
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

    val showBar = navigator.current !is AppDestination.QuizPlay
    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = navigator.tab == tab,
                            onClick = { navigator.openTab(tab) },
                            icon = { Icon(tab.icon(), contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        androidx.compose.foundation.layout.Box(Modifier.padding(padding)) {
            SignedInFlow(navigator) {
                navigator.replace(AppDestination.Welcome)
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
private fun SignedInFlow(navigator: AppNavigator, onLoggedOut: () -> Unit) {
    when (val dest = navigator.current) {
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
        AppDestination.More -> MoreMenuScreen(navigator)
        AppDestination.Welcome, AppDestination.Login, AppDestination.Register -> HomeScreen(navigator)
    }
}

private fun MainTab.icon(): ImageVector = when (this) {
    MainTab.Home -> Icons.Default.Home
    MainTab.Learn -> Icons.Default.Favorite
    MainTab.Quiz -> Icons.Default.Star
    MainTab.Structures -> Icons.Default.Place
    MainTab.More -> Icons.Default.Menu
}
