package app.partners.pnsa.core.di

import androidx.compose.runtime.staticCompositionLocalOf
import app.partners.pnsa.core.data.CatalogCache
import app.partners.pnsa.core.data.ScreenStore
import app.partners.pnsa.core.data.createKeyValueStore
import app.partners.pnsa.core.network.ApiClient
import app.partners.pnsa.core.session.SessionRepository
import app.partners.pnsa.features.auth.data.AuthRepository
import app.partners.pnsa.features.content.data.CatalogRepository
import app.partners.pnsa.features.forum.data.CommunityRepository
import app.partners.pnsa.features.quiz.data.QuizAttemptRepository
import app.partners.pnsa.features.structure.data.OrientationRepository
import app.partners.pnsa.features.user.data.ProfileRepository

class AppGraph {
    val session: SessionRepository = SessionRepository(createKeyValueStore())
    val cache: CatalogCache = CatalogCache()
    val screens: ScreenStore = ScreenStore()
    val api: ApiClient = ApiClient(session)
    val auth: AuthRepository = AuthRepository(api, session)
    val catalog: CatalogRepository = CatalogRepository(api, cache, session)
    val quizAttempts: QuizAttemptRepository = QuizAttemptRepository(api)
    val orientations: OrientationRepository = OrientationRepository(api)
    val community: CommunityRepository = CommunityRepository(api)
    val profile: ProfileRepository = ProfileRepository(api, session)
}

val LocalAppGraph = staticCompositionLocalOf<AppGraph> {
    error("AppGraph n’est pas fourni")
}
