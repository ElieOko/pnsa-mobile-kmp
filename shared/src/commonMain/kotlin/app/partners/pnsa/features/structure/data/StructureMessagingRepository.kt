package app.partners.pnsa.features.structure.data

import app.partners.pnsa.features.structure.domain.models.HealthStructure

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

class StructureMessagingRepository {
    private var nextId: Long = 1L
    private val threads: MutableMap<Long, MutableList<StructureChatMessage>> = mutableMapOf()

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
        return updated
    }

    fun sendToStructure(structure: HealthStructure, text: String): List<StructureChatMessage> {
        val structureId = structure.id ?: return emptyList()
        val thread = threadFor(structure)
        thread += newMessage(structureId, fromStructure = false, text = text.trim(), read = true)
        thread += newMessage(structureId, fromStructure = true, text = autoReply(structure), read = false)
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

    private fun autoReply(structure: HealthStructure): String {
        val firstService = structure.services
            ?.split(".")
            ?.firstOrNull()
            ?.trim()
            ?.ifBlank { null }
        val serviceHint = firstService?.let { " Service prioritaire: $it." }.orEmpty()
        return "Message reçu par ${structure.displayName}.$serviceHint En cas d'urgence, présentez-vous directement à ${structure.displayAddress}."
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
}
