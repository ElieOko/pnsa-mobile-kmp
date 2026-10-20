package app.partners.pnsa.features.structure.data

import app.partners.pnsa.core.data.KeyValueStore
import app.partners.pnsa.core.network.AppJson
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
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

@Serializable
private data class PersistedThread(
    val structureId: Long,
    val messages: List<StructureChatMessage>,
)

@Serializable
private data class PersistedMessagingState(
    val nextId: Long,
    val threads: List<PersistedThread>,
)

class StructureMessagingRepository(
    private val store: KeyValueStore,
) {
    private var nextId: Long = 1L
    private val threads: MutableMap<Long, MutableList<StructureChatMessage>> = mutableMapOf()

    init {
        restore()
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
        persist()
        return updated
    }

    fun sendToStructure(structure: HealthStructure, text: String): List<StructureChatMessage> {
        val structureId = structure.id ?: return emptyList()
        val thread = threadFor(structure)
        thread += newMessage(structureId, fromStructure = false, text = text.trim(), read = true)
        persist()
        return thread.toList()
    }

    private fun threadFor(structure: HealthStructure): MutableList<StructureChatMessage> {
        val structureId = structure.id ?: return mutableListOf()
        return threads.getOrPut(structureId) { mutableListOf() }
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

    private fun persist() {
        val payload = PersistedMessagingState(
            nextId = nextId,
            threads = threads.map { (structureId, messages) ->
                PersistedThread(structureId = structureId, messages = messages.toList())
            },
        )
        store.put(KEY_MESSAGING_STATE, AppJson.encodeToString(payload))
    }

    private fun restore() {
        val raw = store.get(KEY_MESSAGING_STATE) ?: return
        val state = runCatching { AppJson.decodeFromString<PersistedMessagingState>(raw) }.getOrNull() ?: return
        nextId = state.nextId.coerceAtLeast(1L)
        threads.clear()
        state.threads.forEach { thread ->
            threads[thread.structureId] = thread.messages.toMutableList()
        }
        val maxKnownId = threads.values.asSequence().flatten().maxOfOrNull { it.id } ?: 0L
        if (nextId <= maxKnownId) {
            nextId = maxKnownId + 1
        }
    }

    companion object {
        private const val KEY_MESSAGING_STATE = "pref_structure_messaging_state"
    }
}
