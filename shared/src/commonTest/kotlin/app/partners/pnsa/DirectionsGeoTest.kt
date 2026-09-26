package app.partners.pnsa

import app.partners.pnsa.core.location.LatLngPoint
import app.partners.pnsa.core.location.bearingDegrees
import app.partners.pnsa.core.location.decodePolyline
import app.partners.pnsa.core.location.haversineMeters
import app.partners.pnsa.core.location.shouldRefreshRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DirectionsGeoTest {

    @Test
    fun decodesGoogleEncodedPolyline() {
        val points = decodePolyline("_p~iF~ps|U_ulLnnqC_mqNvxq`@")
        assertEquals(3, points.size)
        assertEquals(38.5, points[0].latitude, 0.01)
        assertEquals(-120.2, points[0].longitude, 0.01)
        assertEquals(40.7, points[1].latitude, 0.01)
        assertEquals(-120.95, points[1].longitude, 0.01)
    }

    @Test
    fun refreshesRouteAfterMeaningfulMove() {
        val origin = LatLngPoint(-4.3276, 15.3136)
        assertTrue(shouldRefreshRoute(null, origin))
        assertFalse(shouldRefreshRoute(origin, origin))
        val nearby = LatLngPoint(-4.3277, 15.3137)
        assertFalse(shouldRefreshRoute(origin, nearby, minMeters = 80.0))
        assertTrue(haversineMeters(origin, LatLngPoint(-4.3400, 15.3300)) > 80.0)
        assertTrue(shouldRefreshRoute(origin, LatLngPoint(-4.3400, 15.3300)))
    }

    @Test
    fun bearingPointsEastward() {
        val from = LatLngPoint(-4.3276, 15.3136)
        val to = LatLngPoint(-4.3276, 15.3236)
        val bearing = bearingDegrees(from, to)
        assertTrue(bearing in 80f..100f, "bearing=$bearing")
    }
}
