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

    var mapStyle: String
        get() = store.get(MAP_STYLE) ?: "Standard"
        set(value) { store.put(MAP_STYLE, value) }

    var mapQuery: String
        get() = store.get(MAP_QUERY).orEmpty()
        set(value) { store.put(MAP_QUERY, value) }

    var mapCity: String
        get() = store.get(MAP_CITY).orEmpty()
        set(value) { store.put(MAP_CITY, value) }

    var mapSelectedId: Long?
        get() = store.get(MAP_SELECTED)?.toLongOrNull()
        set(value) { store.put(MAP_SELECTED, value?.toString()) }

    var mapServiceFilter: String
        get() = store.get(MAP_SERVICE_FILTER) ?: "Tous"
        set(value) { store.put(MAP_SERVICE_FILTER, value) }

    var mapTypeFilter: String
        get() = store.get(MAP_TYPE_FILTER) ?: "Toutes"
        set(value) { store.put(MAP_TYPE_FILTER, value) }

    var mapOnlyMode: Boolean
        get() = store.get(MAP_ONLY_MODE) == "1"
        set(value) { store.put(MAP_ONLY_MODE, if (value) "1" else "0") }

    var navTab: String
        get() = store.get(NAV_TAB) ?: "Structures"
        set(value) { store.put(NAV_TAB, value) }

    var navStacksJson: String?
        get() = store.get(NAV_STACKS)
        set(value) { store.put(NAV_STACKS, value) }

    var chatThreadsJson: String?
        get() = store.get(CHAT_THREADS)
        set(value) { store.put(CHAT_THREADS, value) }

    var chatDraftsJson: String?
        get() = store.get(CHAT_DRAFTS)
        set(value) { store.put(CHAT_DRAFTS, value) }

    var chatSearchQuery: String
        get() = store.get(CHAT_SEARCH_QUERY).orEmpty()
        set(value) { store.put(CHAT_SEARCH_QUERY, value) }

    var chatOnlyUnread: Boolean
        get() = store.get(CHAT_ONLY_UNREAD) == "1"
        set(value) { store.put(CHAT_ONLY_UNREAD, if (value) "1" else "0") }

    fun getString(key: String): String? = store.get(key)
    fun putString(key: String, value: String?) = store.put(key, value)

    var lastQuizId: Long?
        get() = store.get(LAST_QUIZ)?.toLongOrNull()
        set(value) { store.put(LAST_QUIZ, value?.toString()) }

    var lastQuizQuestionIndex: Int
        get() = store.get(LAST_QUIZ_INDEX)?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        set(value) { store.put(LAST_QUIZ_INDEX, value.coerceAtLeast(0).toString()) }

    var contentCategory: String
        get() = store.get(CONTENT_CATEGORY) ?: "Tous"
        set(value) { store.put(CONTENT_CATEGORY, value) }

    var contentQuery: String
        get() = store.get(CONTENT_QUERY).orEmpty()
        set(value) { store.put(CONTENT_QUERY, value) }

    companion object {
        private const val FORUM_CATEGORY = "pref_forum_category"
        private const val MAP_SHOW = "pref_map_show"
        private const val MAP_STYLE = "pref_map_style"
        private const val MAP_QUERY = "pref_map_query"
        private const val MAP_CITY = "pref_map_city"
        private const val MAP_SELECTED = "pref_map_selected"
        private const val MAP_SERVICE_FILTER = "pref_map_service_filter"
        private const val MAP_TYPE_FILTER = "pref_map_type_filter"
        private const val MAP_ONLY_MODE = "pref_map_only_mode"
        private const val NAV_TAB = "pref_nav_tab"
        private const val NAV_STACKS = "pref_nav_stacks"
        private const val CHAT_THREADS = "pref_chat_threads"
        private const val CHAT_DRAFTS = "pref_chat_drafts"
        private const val CHAT_SEARCH_QUERY = "pref_chat_search_query"
        private const val CHAT_ONLY_UNREAD = "pref_chat_only_unread"
        private const val LAST_QUIZ = "pref_last_quiz"
        private const val LAST_QUIZ_INDEX = "pref_last_quiz_index"
        private const val CONTENT_CATEGORY = "pref_content_category"
        private const val CONTENT_QUERY = "pref_content_query"
    }
}
