package app.partners.pnsa.features.structure.data

import app.partners.pnsa.core.data.KeyValueStore
import app.partners.pnsa.core.network.AppJson
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString

@Serializable
data class StructureChatMessage(
    val id: Long,
    val structureId: Long,
    val text: String,
    val fromStructure: Boolean,
    val timeLabel: String,
    val read: Boolean,
)

data class StructureChatPreview(
    val structureId: Long,
    val structureName: String,
    val city: String,
    val lastMessage: String,
    val lastTime: String,
    val unreadCount: Int,
)

class StructureMessagingRepository(
    private val store: KeyValueStore? = null,
) {
    private var nextId: Long = 1L
    private val threads: MutableMap<Long, MutableList<StructureChatMessage>> = mutableMapOf()
    private val drafts: MutableMap<Long, String> = mutableMapOf()

    init {
        loadFromStore()
    }

    private fun loadFromStore() {
        val s = store ?: return
        val rawThreads = s.get(KEY_THREADS)
        if (!rawThreads.isNullOrBlank()) {
            runCatching {
                val serializer = MapSerializer(Long.serializer(), ListSerializer(StructureChatMessage.serializer()))
                val decoded = AppJson.decodeFromString(serializer, rawThreads)
                threads.clear()
                var maxId = 0L
                decoded.forEach { (k, list) ->
                    threads[k] = list.toMutableList()
                    list.forEach { if (it.id > maxId) maxId = it.id }
                }
                nextId = maxId + 1L
            }
        }
        val rawDrafts = s.get(KEY_DRAFTS)
        if (!rawDrafts.isNullOrBlank()) {
            runCatching {
                val serializer = MapSerializer(Long.serializer(), String.serializer())
                val decoded = AppJson.decodeFromString(serializer, rawDrafts)
                drafts.clear()
                drafts.putAll(decoded)
            }
        }
    }

    private fun saveThreadsToStore() {
        val s = store ?: return
        runCatching {
            val serializer = MapSerializer(Long.serializer(), ListSerializer(StructureChatMessage.serializer()))
            val snapshot = threads.mapValues { it.value.toList() }
            s.put(KEY_THREADS, AppJson.encodeToString(serializer, snapshot))
        }
    }

    private fun saveDraftsToStore() {
        val s = store ?: return
        runCatching {
            val serializer = MapSerializer(Long.serializer(), String.serializer())
            s.put(KEY_DRAFTS, AppJson.encodeToString(serializer, drafts.toMap()))
        }
    }

    fun draftFor(structureId: Long): String = drafts[structureId].orEmpty()

    fun saveDraft(structureId: Long, draft: String) {
        if (draft.isBlank()) {
            drafts.remove(structureId)
        } else {
            drafts[structureId] = draft
        }
        saveDraftsToStore()
    }

    fun previews(
        structures: List<HealthStructure>,
        includeWithoutMessages: Boolean,
    ): List<StructureChatPreview> {
        val knownStructures = structures.filter { it.id != null }
        return knownStructures.mapNotNull { structure ->
            val structureId = structure.id ?: return@mapNotNull null
            if (!includeWithoutMessages && structureId !in threads.keys) return@mapNotNull null
            val last = threads[structureId]?.lastOrNull()
            StructureChatPreview(
                structureId = structureId,
                structureName = structure.displayName,
                city = structure.displayCity,
                lastMessage = last?.text ?: "Touchez pour démarrer la conversation",
                lastTime = last?.timeLabel.orEmpty(),
                unreadCount = threads[structureId]
                    ?.count { message -> message.fromStructure && !message.read }
                    ?: 0,
            )
        }.sortedWith(
            compareByDescending<StructureChatPreview> { preview ->
                threads[preview.structureId]?.lastOrNull()?.id ?: 0L
            }.thenBy { it.structureName.lowercase() },
        )
    }

    fun messagesFor(structure: HealthStructure): List<StructureChatMessage> {
        val structureId = structure.id ?: return emptyList()
        val thread = threadFor(structure)
        val updated = thread.map { message ->
            if (message.fromStructure && !message.read) {
                message.copy(read = true)
            } else {
                message
            }
        }
        threads[structureId] = updated.toMutableList()
        saveThreadsToStore()
        return updated
    }

    fun sendToStructure(structure: HealthStructure, text: String): List<StructureChatMessage> {
        val structureId = structure.id ?: return emptyList()
        val thread = threadFor(structure)
        thread += newMessage(structureId, fromStructure = false, text = text.trim(), read = true)
        saveDraft(structureId, "")
        saveThreadsToStore()
        return thread.toList()
    }

    private fun threadFor(structure: HealthStructure): MutableList<StructureChatMessage> {
        val structureId = structure.id ?: return mutableListOf()
        return threads.getOrPut(structureId) {
            mutableListOf(
                newMessage(
                    structureId = structureId,
                    fromStructure = true,
                    text = "Bonjour, vous êtes en ligne avec ${structure.displayName}. Décrivez votre besoin et un point focal vous répondra.",
                    read = false,
                ),
            )
        }
    }

    private fun newMessage(
        structureId: Long,
        fromStructure: Boolean,
        text: String,
        read: Boolean,
    ): StructureChatMessage {
        val id = nextId++
        val minute = (id % 60).toInt().toString().padStart(2, '0')
        return StructureChatMessage(
            id = id,
            structureId = structureId,
            text = text,
            fromStructure = fromStructure,
            timeLabel = "14:$minute",
            read = read,
        )
    }

    companion object {
        private const val KEY_THREADS = "persisted_structure_chat_threads"
        private const val KEY_DRAFTS = "persisted_structure_chat_drafts"
    }
}
