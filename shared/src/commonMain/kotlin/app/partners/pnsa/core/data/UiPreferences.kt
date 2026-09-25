package app.partners.pnsa.core.data

class UiPreferences(
    private val store: KeyValueStore,
) {
    var forumCategory: String
        get() = store.get(FORUM_CATEGORY) ?: "Tous"
        set(value) { store.put(FORUM_CATEGORY, value) }

    var mapShowMap: Boolean
        get() = store.get(MAP_SHOW) != "0"
        set(value) { store.put(MAP_SHOW, if (value) "1" else "0") }

    var mapQuery: String
        get() = store.get(MAP_QUERY).orEmpty()
        set(value) { store.put(MAP_QUERY, value) }

    var mapCity: String
        get() = store.get(MAP_CITY).orEmpty()
        set(value) { store.put(MAP_CITY, value) }

    var mapSelectedId: Long?
        get() = store.get(MAP_SELECTED)?.toLongOrNull()
        set(value) { store.put(MAP_SELECTED, value?.toString()) }

    var lastQuizId: Long?
        get() = store.get(LAST_QUIZ)?.toLongOrNull()
        set(value) { store.put(LAST_QUIZ, value?.toString()) }

    companion object {
        private const val FORUM_CATEGORY = "pref_forum_category"
        private const val MAP_SHOW = "pref_map_show"
        private const val MAP_QUERY = "pref_map_query"
        private const val MAP_CITY = "pref_map_city"
        private const val MAP_SELECTED = "pref_map_selected"
        private const val LAST_QUIZ = "pref_last_quiz"
    }
}
