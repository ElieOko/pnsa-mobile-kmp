package app.partners.pnsa

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.di.AppGraph
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.ui.components.KeepAlivePane
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.TikTokBottomBar
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.navigation.MainTab
import app.partners.pnsa.core.ui.navigation.SystemBackHandler
import app.partners.pnsa.core.ui.navigation.exitApplication
import app.partners.pnsa.core.ui.theme.LocalEmbeddedChrome
import app.partners.pnsa.core.ui.theme.LocalTabActive
import app.partners.pnsa.core.ui.theme.PnsaInk
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
import kotlinx.coroutines.delay

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
    var exitPending by remember { mutableStateOf(false) }

    LaunchedEffect(navigator.tab, navigator.current) {
        if (navigator.tab !in visited) visited += navigator.tab
        exitPending = false
        navigator.saveTo(graph.prefs)
    }

    LaunchedEffect(exitPending) {
        if (exitPending) {
            delay(2000L)
            exitPending = false
        }
    }

    fun handleBackPress() {
        val navigated = navigator.navigateBack()
        if (navigated) {
            exitPending = false
        } else if (exitPending) {
            navigator.saveTo(graph.prefs)
            exitApplication()
        } else {
            exitPending = true
        }
    }

    SystemBackHandler(enabled = true) {
        handleBackPress()
    }

    val isChatConversation = when (navigator.current) {
        is AppDestination.StructureChat,
        is AppDestination.AdviceChat,
        -> true
        else -> false
    }

    CompositionLocalProvider(LocalEmbeddedChrome provides true) {
        PnsaScaffold(
            containerColor = Color.White,
            bottomBar = {
                if (!isChatConversation) {
                    TikTokBottomBar(
                        selected = navigator.tab,
                        onSelect = { navigator.openTab(it) },
                    )
                }
            },
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .clipToBounds(),
            ) {
                MainTab.entries.forEach { tab ->
                    if (tab in visited) {
                        KeepAlivePane(visible = navigator.tab == tab) {
                            CompositionLocalProvider(LocalTabActive provides (navigator.tab == tab)) {
                                SignedInFlow(navigator, tab, onBack = { handleBackPress() })
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = exitPending,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = PnsaInk.copy(alpha = 0.90f),
                        shadowElevation = 4.dp,
                    ) {
                        Text(
                            text = "Appuie encore une fois sur retour pour quitter l’application",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignedInFlow(navigator: AppNavigator, tab: MainTab, onBack: () -> Unit) {
    when (val dest = navigator.currentFor(tab)) {
        AppDestination.Structures -> StructureListScreen(navigator)
        is AppDestination.StructureDetail -> StructureDetailScreen(dest.id, navigator, onBack = onBack)
        AppDestination.Messages -> StructureConversationsScreen(navigator)
        is AppDestination.StructureChat -> StructureChatScreen(dest.structureId, onBack = onBack)
        AppDestination.Advice -> AdviceListScreen(navigator)
        is AppDestination.AdviceChat -> AdviceChatScreen(dest.id, onBack = onBack)
        AppDestination.Orientations -> OrientationListScreen(navigator)
        is AppDestination.OrientationDetail -> OrientationDetailScreen(dest.id, onBack = onBack)
        is AppDestination.OrientationCreate -> OrientationCreateScreen(dest.structureId, navigator, onBack = onBack)
        AppDestination.Learn -> ContentListScreen(navigator)
        is AppDestination.ContentDetail -> ContentDetailScreen(dest.id, onBack = onBack)
        AppDestination.Profile, AppDestination.More -> ProfileScreen(navigator)
        AppDestination.ProfileEdit -> ProfileEditScreen(onBack = onBack)
        AppDestination.Help, AppDestination.PublicHelp -> HelpScreen(onBack = onBack)
        AppDestination.PublicLegal -> LegalScreen(onBack = onBack)
        AppDestination.Home,
        AppDestination.Welcome,
        AppDestination.Login,
        AppDestination.Register,
        -> StructureListScreen(navigator)
    }
}
