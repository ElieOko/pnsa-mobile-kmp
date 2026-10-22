package app.partners.pnsa.core.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import app.partners.pnsa.core.data.UiPreferences
import app.partners.pnsa.core.network.AppJson
import kotlinx.serialization.encodeToString

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
    private val onStateChanged: (() -> Unit)? = null,
) {
    private val authStack = mutableStateListOf(start)
    private val tabStacks: Map<MainTab, SnapshotStateList<AppDestination>> =
        MainTab.entries.associateWith { tab -> mutableStateListOf(rootFor(tab)) }
    private val tabHistory = mutableStateListOf<MainTab>()

    var tab by mutableStateOf(tabFor(start) ?: MainTab.Structures)
        private set
    var signedIn by mutableStateOf(true)

    init {
        val startTab = tabFor(start)
        if (startTab != null && !isTabRoot(start)) {
            val stack = tabStacks.getValue(startTab)
            if (stack.lastOrNull() != start) stack += start
        }
    }

    val current: AppDestination
        get() = if (signedIn) tabStacks.getValue(tab).last() else authStack.last()
    val canPop: Boolean
        get() = if (signedIn) tabStacks.getValue(tab).size > 1 else authStack.size > 1

    fun currentFor(tab: MainTab): AppDestination = tabStacks.getValue(tab).last()

    fun stackFor(tab: MainTab): List<AppDestination> = tabStacks.getValue(tab).toList()

    fun restoreState(savedTab: MainTab, savedStacks: Map<MainTab, List<AppDestination>>) {
        savedStacks.forEach { (t, dests) ->
            val stack = tabStacks[t] ?: return@forEach
            stack.clear()
            val valid = dests.ifEmpty { listOf(rootFor(t)) }
            if (valid.first() != rootFor(t)) {
                stack += rootFor(t)
            }
            valid.forEach { d ->
                if (stack.lastOrNull() != d) stack += d
            }
        }
        tab = savedTab
    }

    fun saveTo(prefs: UiPreferences) {
        prefs.navTab = tab.name
        val rawMap: Map<String, List<String>> = MainTab.entries.associate { t ->
            t.name to stackFor(t).mapNotNull { encodeDestination(it) }
        }
        runCatching { AppJson.encodeToString(rawMap) }.onSuccess { prefs.navStacksJson = it }
    }

    fun restoreFrom(prefs: UiPreferences) {
        val savedTab = MainTab.entries.firstOrNull { it.name == prefs.navTab } ?: MainTab.Structures
        val rawJson = prefs.navStacksJson.orEmpty()
        val savedStacks: Map<MainTab, List<AppDestination>> = if (rawJson.isNotBlank()) {
            runCatching { AppJson.decodeFromString<Map<String, List<String>>>(rawJson) }
                .getOrNull()
                ?.mapNotNull { (key, list) ->
                    val t = MainTab.entries.firstOrNull { it.name == key } ?: return@mapNotNull null
                    t to list.mapNotNull { decodeDestination(it) }
                }
                ?.toMap()
                ?: emptyMap()
        } else {
            emptyMap()
        }
        restoreState(savedTab, savedStacks)
    }

    fun push(destination: AppDestination) {
        val target = tabFor(destination)
        if (signedIn && target != null && isTabRoot(destination)) {
            openTab(target)
            return
        }
        if (signedIn) {
            val stackTab = target ?: tab
            if (stackTab != tab) {
                recordTabJump(tab)
                tab = stackTab
            }
            val stack = tabStacks.getValue(stackTab)
            if (stack.lastOrNull() == destination) return
            stack += destination
        } else {
            authStack += destination
        }
        onStateChanged?.invoke()
    }

    fun replace(destination: AppDestination) {
        if (signedIn) {
            val target = tabFor(destination) ?: tab
            if (target != tab) recordTabJump(tab)
            tab = target
            val stack = tabStacks.getValue(target)
            stack.clear()
            stack += destination
        } else {
            authStack.clear()
            authStack += destination
        }
        onStateChanged?.invoke()
    }

    fun pop() {
        navigateBack()
    }

    /**
     * Handles back navigation across screens and tabs.
     * @return true if navigation moved to a previous destination or tab,
     *         false if already at the root of the main tab (ready for double-back exit).
     */
    fun navigateBack(): Boolean {
        val stack = if (signedIn) tabStacks.getValue(tab) else authStack
        if (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
            onStateChanged?.invoke()
            return true
        }
        if (signedIn) {
            while (tabHistory.isNotEmpty()) {
                val previousTab = tabHistory.removeAt(tabHistory.lastIndex)
                if (previousTab != tab) {
                    tab = previousTab
                    onStateChanged?.invoke()
                    return true
                }
            }
            if (tab != MainTab.Structures) {
                tab = MainTab.Structures
                onStateChanged?.invoke()
                return true
            }
        }
        return false
    }

    fun openTab(next: MainTab) {
        if (tab != next) {
            recordTabJump(tab)
            tab = next
            onStateChanged?.invoke()
        }
    }

    private fun recordTabJump(previous: MainTab) {
        if (tabHistory.lastOrNull() != previous) {
            tabHistory += previous
            if (tabHistory.size > 12) {
                tabHistory.removeAt(0)
            }
        }
    }

    fun replaceCurrent(destination: AppDestination) {
        val stack = if (signedIn) tabStacks.getValue(tab) else authStack
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
        stack += destination
        tabFor(destination)?.let { tab = it }
        onStateChanged?.invoke()
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
        AppDestination.Messages,
        is AppDestination.StructureChat,
        AppDestination.Advice,
        is AppDestination.AdviceChat,
        -> MainTab.Messages
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
    AppDestination.Advice -> "Conseil"
    is AppDestination.AdviceChat -> "Échange"
    AppDestination.Profile -> "Profil"
    AppDestination.ProfileEdit -> "Modifier le profil"
    AppDestination.More -> "Menu"
    AppDestination.Messages -> "Discussions"
    is AppDestination.StructureChat -> "Conversation"
}

private fun encodeDestination(destination: AppDestination): String? = when (destination) {
    AppDestination.Structures -> "Structures"
    is AppDestination.StructureDetail -> "StructureDetail:${destination.id}"
    AppDestination.Messages -> "Messages"
    is AppDestination.StructureChat -> "StructureChat:${destination.structureId}"
    AppDestination.Advice -> "Advice"
    is AppDestination.AdviceChat -> "AdviceChat:${destination.id}"
    AppDestination.Orientations -> "Orientations"
    is AppDestination.OrientationDetail -> "OrientationDetail:${destination.id}"
    is AppDestination.OrientationCreate -> "OrientationCreate:${destination.structureId}"
    AppDestination.Learn -> "Learn"
    is AppDestination.ContentDetail -> "ContentDetail:${destination.id}"
    AppDestination.Profile -> "Profile"
    AppDestination.ProfileEdit -> "ProfileEdit"
    AppDestination.Help -> "Help"
    AppDestination.PublicHelp -> "PublicHelp"
    AppDestination.PublicLegal -> "PublicLegal"
    AppDestination.More -> "More"
    AppDestination.Home -> "Home"
    else -> null
}

private fun decodeDestination(token: String): AppDestination? {
    val parts = token.split(":", limit = 2)
    val kind = parts.firstOrNull().orEmpty()
    val id = parts.getOrNull(1)?.toLongOrNull()
    return when (kind) {
        "Structures" -> AppDestination.Structures
        "StructureDetail" -> id?.let { AppDestination.StructureDetail(it) }
        "Messages" -> AppDestination.Messages
        "StructureChat" -> id?.let { AppDestination.StructureChat(it) }
        "Advice" -> AppDestination.Advice
        "AdviceChat" -> id?.let { AppDestination.AdviceChat(it) }
        "Orientations" -> AppDestination.Orientations
        "OrientationDetail" -> id?.let { AppDestination.OrientationDetail(it) }
        "OrientationCreate" -> id?.let { AppDestination.OrientationCreate(it) }
        "Learn" -> AppDestination.Learn
        "ContentDetail" -> id?.let { AppDestination.ContentDetail(it) }
        "Profile" -> AppDestination.Profile
        "ProfileEdit" -> AppDestination.ProfileEdit
        "Help" -> AppDestination.Help
        "PublicHelp" -> AppDestination.PublicHelp
        "PublicLegal" -> AppDestination.PublicLegal
        "More" -> AppDestination.More
        "Home" -> AppDestination.Home
        else -> null
    }
}
