package app.partners.pnsa.core.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.partners.pnsa.features.structure.domain.models.HealthStructure

@Composable
expect fun PlatformStructureMap(
    structures: List<HealthStructure>,
    selectedId: Long?,
    onSelect: (HealthStructure) -> Unit,
    modifier: Modifier = Modifier,
)

expect fun usesOpenStreetMap(): Boolean
