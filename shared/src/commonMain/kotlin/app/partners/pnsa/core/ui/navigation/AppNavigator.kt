package app.partners.pnsa.core.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class MainTab(val label: String) {
    Home("Accueil"),
    Learn("Apprendre"),
    Quiz("Quiz"),
    Structures("Structures"),
    More("Plus"),
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
    private val stack = mutableStateListOf(start)
    var tab by mutableStateOf(MainTab.Home)

    val current: AppDestination get() = stack.last()
    val canPop: Boolean get() = stack.size > 1

    fun push(destination: AppDestination) {
        stack += destination
        tab = tabFor(destination) ?: tab
    }

    fun replace(destination: AppDestination) {
        stack.clear()
        stack += destination
        tab = tabFor(destination) ?: tab
    }

    fun pop() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    fun openTab(next: MainTab) {
        tab = next
        val dest = when (next) {
            MainTab.Home -> AppDestination.Home
            MainTab.Learn -> AppDestination.Learn
            MainTab.Quiz -> AppDestination.Quizzes
            MainTab.Structures -> AppDestination.Structures
            MainTab.More -> AppDestination.More
        }
        replace(dest)
    }

    fun replaceCurrent(destination: AppDestination) {
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
        stack += destination
        tab = tabFor(destination) ?: tab
    }

    private fun tabFor(destination: AppDestination): MainTab? = when (destination) {
        AppDestination.Home -> MainTab.Home
        AppDestination.Learn, is AppDestination.ContentDetail -> MainTab.Learn
        AppDestination.Quizzes, is AppDestination.QuizPlay -> MainTab.Quiz
        AppDestination.Structures, is AppDestination.StructureDetail -> MainTab.Structures
        AppDestination.More,
        AppDestination.Forum,
        is AppDestination.ForumDetail,
        AppDestination.Advice,
        is AppDestination.AdviceChat,
        AppDestination.Orientations,
        is AppDestination.OrientationDetail,
        is AppDestination.OrientationCreate,
        AppDestination.Profile,
        AppDestination.ProfileEdit,
        AppDestination.Help,
        -> MainTab.More
        else -> null
    }
}
