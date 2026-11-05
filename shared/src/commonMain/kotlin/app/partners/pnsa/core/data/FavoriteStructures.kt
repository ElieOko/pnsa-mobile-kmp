package app.partners.pnsa.core.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class FavoriteStructures(
    private val prefs: UiPreferences,
) {
    var ids by mutableStateOf(read())
        private set

    fun contains(id: Long?): Boolean = id != null && id in ids

    fun toggle(id: Long) {
        val next = ids.toMutableSet()
        if (!next.add(id)) next.remove(id)
        ids = next
        prefs.putString(KEY, next.joinToString(","))
    }

    private fun read(): Set<Long> =
        prefs.getString(KEY)
            .orEmpty()
            .split(',')
            .mapNotNull { it.toLongOrNull() }
            .toSet()

    private companion object {
        const val KEY = "pref_favorite_structures"
    }
}
