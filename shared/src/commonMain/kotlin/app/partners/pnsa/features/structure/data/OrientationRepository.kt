package app.partners.pnsa.features.structure.data

import app.partners.pnsa.core.network.ApiClient
import app.partners.pnsa.core.network.DataWrapper
import app.partners.pnsa.core.network.PageDto
import app.partners.pnsa.core.util.randomUuid
import app.partners.pnsa.features.structure.domain.models.Orientation
import app.partners.pnsa.features.structure.domain.models.OrientationCreateRequest

class OrientationRepository(
    private val api: ApiClient,
) {
    suspend fun list(): PageDto<Orientation> =
        api.get("mobile/v1/orientations", PageDto.serializer(Orientation.serializer()))

    suspend fun detail(id: Long): Orientation =
        api.get("mobile/v1/orientations/$id", DataWrapper.serializer(Orientation.serializer())).data

    suspend fun create(
        structureId: Long,
        reason: String?,
        clientRequestId: String = randomUuid(),
    ): Orientation {
        return api.post(
            path = "mobile/v1/orientations",
            serializer = DataWrapper.serializer(Orientation.serializer()),
            body = OrientationCreateRequest(
                healthStructureId = structureId,
                clientRequestId = clientRequestId,
                reason = reason?.takeIf { it.isNotBlank() },
            ),
        ).data
    }
}
