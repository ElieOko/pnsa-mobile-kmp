package app.partners.pnsa.features.home.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.partners.pnsa.core.data.UiPreferences

class RepereSearch(
    private val prefs: UiPreferences,
) {
    var serviceFilter by mutableStateOf(
        prefs.mapServiceFilter
            .let { saved -> if (saved == "IST et VIH") "IST et VIH/SIDA" else saved }
            .takeIf { saved -> RepereFiltres.any { it.label == saved } } ?: "Tous",
    )
    var city by mutableStateOf(prefs.mapCity)
    var query by mutableStateOf(prefs.mapQuery)
    var request by mutableIntStateOf(0)
        private set
    var openOnMap by mutableStateOf(true)
        private set

    fun updateQuery(value: String) {
        query = value
        prefs.mapQuery = value
    }

    fun updateCity(value: String) {
        city = value
        prefs.mapCity = value
    }

    fun updateFilter(label: String) {
        val safe = label.takeIf { saved -> RepereFiltres.any { it.label == saved } } ?: "Tous"
        serviceFilter = safe
        prefs.mapServiceFilter = safe
    }

    fun open(filter: String = "Tous", city: String = this.city, query: String = "", map: Boolean = true) {
        updateFilter(filter)
        updateCity(city)
        updateQuery(query)
        openOnMap = map
        request++
    }
}
