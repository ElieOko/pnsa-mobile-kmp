package app.partners.pnsa.core.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

enum class MainTab(val label: String) {
    Home("Accueil"),
    Learn("Apprendre"),
    Quiz("Quiz"),
    Structures("Carte"),
    Forum("Forum"),
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
    data object Quizzes : AppDestination()
    data class QuizPlay(val id: Long) : AppDestination()
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
}

class AppNavigator(
    start: AppDestination,
) {
    private val authStack = mutableStateListOf(start)
    private val tabStacks: Map<MainTab, SnapshotStateList<AppDestination>> =
        MainTab.entries.associateWith { tab -> mutableStateListOf(rootFor(tab)) }

    var tab by mutableStateOf(MainTab.Home)
        private set
    var signedIn by mutableStateOf(start is AppDestination.Home || tabFor(start) != null)

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
            stack += if (isTabRoot(destination)) destination else destination
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
        MainTab.Home -> AppDestination.Home
        MainTab.Learn -> AppDestination.Learn
        MainTab.Quiz -> AppDestination.Quizzes
        MainTab.Structures -> AppDestination.Structures
        MainTab.Forum -> AppDestination.Forum
    }

    private fun isTabRoot(destination: AppDestination): Boolean = when (destination) {
        AppDestination.Home,
        AppDestination.Learn,
        AppDestination.Quizzes,
        AppDestination.Structures,
        AppDestination.Forum,
        -> true
        else -> false
    }

    fun tabFor(destination: AppDestination): MainTab? = when (destination) {
        AppDestination.Home -> MainTab.Home
        AppDestination.Learn, is AppDestination.ContentDetail -> MainTab.Learn
        AppDestination.Quizzes, is AppDestination.QuizPlay -> MainTab.Quiz
        AppDestination.Structures, is AppDestination.StructureDetail -> MainTab.Structures
        AppDestination.Forum, is AppDestination.ForumDetail -> MainTab.Forum
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
    AppDestination.Quizzes -> "Quiz"
    is AppDestination.QuizPlay -> "Quiz"
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
}
