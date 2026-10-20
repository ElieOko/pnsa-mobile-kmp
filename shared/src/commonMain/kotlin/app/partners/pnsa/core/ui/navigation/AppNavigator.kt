package app.partners.pnsa.core.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

enum class MainTab(val label: String) {
    Structures("Carte"),
    Messages("Discussions"),
    Profile("Profil"),
}

sealed class AppDestination {
    data object Welcome : AppDestination()
    data object Login : AppDestination()
    data object Register : AppDestination()
    data object PublicHelp : AppDestination()
    data object PublicLegal : AppDestination()

    data object Home : AppDestination()
    data object Learn : AppDestination()
    data class ContentDetail(val id: Long) : AppDestination()
    data object Structures : AppDestination()
    data class StructureDetail(val id: Long) : AppDestination()
    data object Orientations : AppDestination()
    data class OrientationDetail(val id: Long) : AppDestination()
    data class OrientationCreate(val structureId: Long) : AppDestination()
    data object Forum : AppDestination()
    data class ForumDetail(val id: Long) : AppDestination()
    data object Advice : AppDestination()
    data class AdviceChat(val id: Long) : AppDestination()
    data object Profile : AppDestination()
    data object ProfileEdit : AppDestination()
    data object Help : AppDestination()
    data object More : AppDestination()
    data object Messages : AppDestination()
    data class StructureChat(val structureId: Long) : AppDestination()
}

class AppNavigator(
    start: AppDestination = AppDestination.Structures,
) {
    private val authStack = mutableStateListOf(start)
    private val tabStacks: Map<MainTab, SnapshotStateList<AppDestination>> =
        MainTab.entries.associateWith { tab -> mutableStateListOf(rootFor(tab)) }

    var tab by mutableStateOf(tabFor(start) ?: MainTab.Structures)
        private set
    var signedIn by mutableStateOf(true)

    val current: AppDestination
        get() = if (signedIn) tabStacks.getValue(tab).last() else authStack.last()
    val canPop: Boolean
        get() = if (signedIn) tabStacks.getValue(tab).size > 1 else authStack.size > 1

    fun currentFor(tab: MainTab): AppDestination = tabStacks.getValue(tab).last()

    fun push(destination: AppDestination) {
        val target = tabFor(destination)
        if (signedIn && target != null && isTabRoot(destination)) {
            openTab(target)
            return
        }
        if (signedIn) {
            val stackTab = target ?: tab
            if (stackTab != tab) tab = stackTab
            val stack = tabStacks.getValue(stackTab)
            if (stack.lastOrNull() == destination) return
            stack += destination
        } else {
            authStack += destination
        }
    }

    fun replace(destination: AppDestination) {
        if (signedIn) {
            val target = tabFor(destination) ?: tab
            tab = target
            val stack = tabStacks.getValue(target)
            stack.clear()
            stack += destination
        } else {
            authStack.clear()
            authStack += destination
        }
    }

    fun pop() {
        val stack = if (signedIn) tabStacks.getValue(tab) else authStack
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    fun openTab(next: MainTab) {
        tab = next
    }

    fun replaceCurrent(destination: AppDestination) {
        val stack = if (signedIn) tabStacks.getValue(tab) else authStack
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
        stack += destination
        tabFor(destination)?.let { tab = it }
    }

    fun title(): String = titleFor(current)

    private fun rootFor(tab: MainTab): AppDestination = when (tab) {
        MainTab.Structures -> AppDestination.Structures
        MainTab.Messages -> AppDestination.Messages
        MainTab.Profile -> AppDestination.Profile
    }

    private fun isTabRoot(destination: AppDestination): Boolean = when (destination) {
        AppDestination.Structures,
        AppDestination.Messages,
        AppDestination.Profile,
        -> true
        else -> false
    }

    fun tabFor(destination: AppDestination): MainTab? = when (destination) {
        AppDestination.Structures, is AppDestination.StructureDetail -> MainTab.Structures
        AppDestination.Messages, is AppDestination.StructureChat -> MainTab.Messages
        AppDestination.Advice, is AppDestination.AdviceChat -> MainTab.Messages
        AppDestination.Profile,
        AppDestination.ProfileEdit,
        AppDestination.Orientations,
        is AppDestination.OrientationDetail,
        is AppDestination.OrientationCreate,
        AppDestination.Help,
        AppDestination.More,
        AppDestination.PublicHelp,
        AppDestination.PublicLegal,
        AppDestination.Learn,
        is AppDestination.ContentDetail,
        AppDestination.Home,
        -> MainTab.Profile
        else -> null
    }
}

fun titleFor(destination: AppDestination): String = when (destination) {
    AppDestination.Welcome -> "PNSA"
    AppDestination.Login -> "Connexion"
    AppDestination.Register -> "Inscription"
    AppDestination.PublicHelp, AppDestination.Help -> "Aide"
    AppDestination.PublicLegal -> "Confidentialité"
    AppDestination.Home -> "PNSA"
    AppDestination.Learn -> "Apprendre"
    is AppDestination.ContentDetail -> "Contenu"
    AppDestination.Structures -> "Carte"
    is AppDestination.StructureDetail -> "Structure"
    AppDestination.Orientations -> "Orientations"
    is AppDestination.OrientationDetail -> "Orientation"
    is AppDestination.OrientationCreate -> "Nouvelle orientation"
    AppDestination.Forum -> "Forum"
    is AppDestination.ForumDetail -> "Discussion"
    AppDestination.Advice -> "Conseil"
    is AppDestination.AdviceChat -> "Échange"
    AppDestination.Profile -> "Profil"
    AppDestination.ProfileEdit -> "Modifier le profil"
    AppDestination.More -> "Menu"
    AppDestination.Messages -> "Discussions"
    is AppDestination.StructureChat -> "Conversation"
}
