package app.partners.pnsa.features.forum.data

import app.partners.pnsa.core.network.ApiClient
import app.partners.pnsa.core.network.DataWrapper
import app.partners.pnsa.core.network.PageDto
import app.partners.pnsa.core.network.query
import app.partners.pnsa.features.auth.domain.models.CreateCommentRequest
import app.partners.pnsa.features.auth.domain.models.CreateMessageRequest
import app.partners.pnsa.features.forum.domain.models.Commentaire
import app.partners.pnsa.features.forum.domain.models.Discussion
import app.partners.pnsa.features.forum.domain.models.Message
import app.partners.pnsa.features.forum.domain.models.Sujet

class CommunityRepository(
    private val api: ApiClient,
) {
    suspend fun sujets(page: Int = 1, perPage: Int = 20): PageDto<Sujet> =
        api.get("mobile/v1/forum/sujets", PageDto.serializer(Sujet.serializer())) {
            query("page", page)
            query("per_page", perPage)
        }

    suspend fun sujet(id: Long): Sujet =
        api.get("mobile/v1/forum/sujets/$id", DataWrapper.serializer(Sujet.serializer())).data

    suspend fun comment(sujetId: Long, text: String): Commentaire =
        api.post(
            path = "mobile/v1/forum/sujets/$sujetId/commentaires",
            serializer = DataWrapper.serializer(Commentaire.serializer()),
            body = CreateCommentRequest(commentaire = text),
        ).data

    suspend fun discussions(): PageDto<Discussion> =
        api.get("mobile/v1/advice/discussions", PageDto.serializer(Discussion.serializer()))

    suspend fun messages(discussionId: Long, page: Int = 1): PageDto<Message> =
        api.get(
            "mobile/v1/advice/discussions/$discussionId/messages",
            PageDto.serializer(Message.serializer()),
        ) {
            query("page", page)
            query("per_page", 50)
        }

    suspend fun sendMessage(discussionId: Long, text: String): Message =
        api.post(
            path = "mobile/v1/advice/discussions/$discussionId/messages",
            serializer = DataWrapper.serializer(Message.serializer()),
            body = CreateMessageRequest(msg = text),
        ).data
}
