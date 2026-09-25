package app.partners.pnsa.core.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.partners.pnsa.core.ui.components.KinshasaGpsMap
import app.partners.pnsa.features.structure.domain.models.HealthStructure

@Composable
actual fun PlatformStructureMap(
    structures: List<HealthStructure>,
    selectedId: Long?,
    onSelect: (HealthStructure) -> Unit,
    modifier: Modifier,
) {
    KinshasaGpsMap(
        structures = structures,
        selectedId = selectedId,
        onSelect = onSelect,
        modifier = modifier,
    )
}

actual fun usesOpenStreetMap(): Boolean = false
