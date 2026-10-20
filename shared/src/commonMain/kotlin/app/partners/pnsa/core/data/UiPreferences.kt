package app.partners.pnsa.core.data

class UiPreferences(
    private val store: KeyValueStore,
) {
    var activeMainTab: String
        get() = store.get(ACTIVE_MAIN_TAB) ?: "Structures"
        set(value) { store.put(ACTIVE_MAIN_TAB, value) }

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

    var mapImmersiveMode: Boolean
        get() = store.get(MAP_IMMERSIVE) == "1"
        set(value) { store.put(MAP_IMMERSIVE, if (value) "1" else "0") }

    var mapServiceFilter: String
        get() = store.get(MAP_SERVICE_FILTER) ?: "Toutes"
        set(value) { store.put(MAP_SERVICE_FILTER, value) }

    var messageSearchQuery: String
        get() = store.get(MESSAGE_QUERY).orEmpty()
        set(value) { store.put(MESSAGE_QUERY, value) }

    var messageOnlyUnread: Boolean
        get() = store.get(MESSAGE_ONLY_UNREAD) == "1"
        set(value) { store.put(MESSAGE_ONLY_UNREAD, if (value) "1" else "0") }

    fun structureChatDraft(structureId: Long): String =
        store.get("$STRUCTURE_DRAFT_PREFIX$structureId").orEmpty()

    fun saveStructureChatDraft(structureId: Long, value: String) {
        val key = "$STRUCTURE_DRAFT_PREFIX$structureId"
        if (value.isBlank()) {
            store.put(key, null)
        } else {
            store.put(key, value)
        }
    }

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
        private const val ACTIVE_MAIN_TAB = "pref_active_main_tab"
        private const val FORUM_CATEGORY = "pref_forum_category"
        private const val MAP_SHOW = "pref_map_show"
        private const val MAP_STYLE = "pref_map_style"
        private const val MAP_QUERY = "pref_map_query"
        private const val MAP_CITY = "pref_map_city"
        private const val MAP_SELECTED = "pref_map_selected"
        private const val MAP_IMMERSIVE = "pref_map_immersive"
        private const val MAP_SERVICE_FILTER = "pref_map_service_filter"
        private const val MESSAGE_QUERY = "pref_message_query"
        private const val MESSAGE_ONLY_UNREAD = "pref_message_only_unread"
        private const val STRUCTURE_DRAFT_PREFIX = "pref_structure_chat_draft_"
        private const val LAST_QUIZ = "pref_last_quiz"
        private const val LAST_QUIZ_INDEX = "pref_last_quiz_index"
        private const val CONTENT_CATEGORY = "pref_content_category"
        private const val CONTENT_QUERY = "pref_content_query"
    }
}
