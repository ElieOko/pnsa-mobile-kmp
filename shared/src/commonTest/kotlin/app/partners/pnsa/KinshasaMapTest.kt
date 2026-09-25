package app.partners.pnsa

import app.partners.pnsa.core.ui.components.KinshasaMapMath
import app.partners.pnsa.features.structure.data.KinshasaCenters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KinshasaMapTest {

    @Test
    fun localCentersHaveValidGps() {
        assertTrue(KinshasaCenters.all.size >= 10)
        KinshasaCenters.all.forEach { center ->
            assertTrue(center.hasCoordinates, center.displayName)
            val lat = center.latitude!!
            val lon = center.longitude!!
            assertTrue(lat in -4.50..-4.29, "${center.displayName} lat=$lat")
            assertTrue(lon in 15.22..15.44, "${center.displayName} lon=$lon")
        }
    }

    @Test
    fun mergeKeepsRemoteAndAddsLocalFallback() {
        val remote = listOf(KinshasaCenters.all.first().copy(id = 7, name = "Hôpital distant"))
        val merged = KinshasaCenters.mergeWith(remote)
        assertTrue(merged.size > remote.size)
        assertTrue(merged.any { it.id == 7L })
    }

    @Test
    fun projectKeepsMarkersOnTheMap() {
        val point = KinshasaMapMath.project(-4.3276, 15.3136, 1000f, 800f)
        assertTrue(point.x in 0f..1000f)
        assertTrue(point.y in 0f..800f)
        assertTrue(KinshasaMapMath.formatCoord(-4.3276, 15.3136).contains("S"))
        assertEquals("Coordonnées indisponibles", KinshasaMapMath.formatCoord(null, null))
    }
}
