package app.partners.pnsa.features.content.data

import app.partners.pnsa.core.data.CatalogCache
import app.partners.pnsa.core.network.ApiClient
import app.partners.pnsa.core.network.DataWrapper
import app.partners.pnsa.core.network.PageDto
import app.partners.pnsa.core.network.query
import app.partners.pnsa.core.session.SessionRepository
import app.partners.pnsa.features.content.domain.models.CatalogSyncPayload
import app.partners.pnsa.features.content.domain.models.Contenu
import app.partners.pnsa.features.content.domain.models.HomeFeed
import app.partners.pnsa.features.quiz.domain.models.Quiz
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import app.partners.pnsa.features.structure.domain.models.StructureSyncPayload

class CatalogRepository(
    private val api: ApiClient,
    private val cache: CatalogCache,
    private val session: SessionRepository,
) {
    suspend fun home(): HomeFeed {
        val feed = api.get("mobile/v1/home", DataWrapper.serializer(HomeFeed.serializer())).data
        cache.putCatalog(
            contenus = feed.contenus,
            actualites = feed.actualites,
            faqs = feed.faqs,
        )
        return feed
    }

    suspend fun syncCatalog(since: String? = session.lastSync()): CatalogSyncPayload {
        val payload = api.get(
            "mobile/v1/sync/catalog",
            DataWrapper.serializer(CatalogSyncPayload.serializer()),
        ) {
            query("since", since)
        }.data
        cache.putCatalog(
            contenus = payload.contenus,
            quizzes = payload.quizzes,
            actualites = payload.actualites,
            faqs = payload.faqs,
            syncedAt = payload.syncedAt ?: payload.cursor,
        )
        session.saveLastSync(payload.cursor ?: payload.syncedAt)
        return payload
    }

    suspend fun syncStructures(since: String? = cache.lastStructureSync): StructureSyncPayload {
        val payload = api.get(
            "mobile/v1/sync/structures",
            DataWrapper.serializer(StructureSyncPayload.serializer()),
        ) {
            query("since", since)
        }.data
        cache.putStructures(payload.structures, payload.syncedAt ?: payload.cursor)
        return payload
    }

    suspend fun contenus(page: Int = 1, perPage: Int = 20): PageDto<Contenu> {
        val result = api.get("mobile/v1/contenus", PageDto.serializer(Contenu.serializer())) {
            query("page", page)
            query("per_page", perPage)
        }
        cache.putCatalog(contenus = result.data)
        return result
    }

    suspend fun contenu(id: Long): Contenu {
        return api.get("mobile/v1/contenus/$id", DataWrapper.serializer(Contenu.serializer())).data
    }

    suspend fun quizzes(page: Int = 1, perPage: Int = 20): PageDto<Quiz> {
        val result = api.get("mobile/v1/quizzes", PageDto.serializer(Quiz.serializer())) {
            query("page", page)
            query("per_page", perPage)
        }
        cache.putCatalog(quizzes = result.data)
        return result
    }

    suspend fun quiz(id: Long): Quiz {
        val quiz = api.get("mobile/v1/quizzes/$id", DataWrapper.serializer(Quiz.serializer())).data
        cache.putCatalog(quizzes = listOf(quiz))
        return quiz
    }

    suspend fun structures(
        page: Int = 1,
        perPage: Int = 20,
        province: String? = null,
        city: String? = null,
        queryText: String? = null,
    ): PageDto<HealthStructure> {
        val result = api.get(
            "mobile/v1/structures",
            PageDto.serializer(HealthStructure.serializer()),
        ) {
            query("page", page)
            query("per_page", perPage)
            query("province", province)
            query("city", city)
            query("q", queryText)
        }
        cache.putStructures(result.data, cache.lastStructureSync)
        return result
    }

    suspend fun structure(id: Long): HealthStructure {
        return api.get(
            "mobile/v1/structures/$id",
            DataWrapper.serializer(HealthStructure.serializer()),
        ).data
    }

    fun cachedContenus(): List<Contenu> = cache.contenus
    fun cachedQuizzes(): List<Quiz> = cache.quizzes
    fun cachedFaqs() = cache.faqs
    fun lastSyncLabel(): String? = cache.lastCatalogSync ?: session.lastSync()
}
