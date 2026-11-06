package app.partners.pnsa.features.structure.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.ui.components.quietClick
import app.partners.pnsa.core.ui.theme.RepereInk
import app.partners.pnsa.core.ui.theme.RepereLavender
import app.partners.pnsa.core.ui.theme.RepereMuted
import app.partners.pnsa.core.ui.theme.ReperePurple
import app.partners.pnsa.core.ui.theme.RepereTeal
import app.partners.pnsa.features.home.data.RepereFiltres
import app.partners.pnsa.features.home.data.espaceJeunes
import app.partners.pnsa.features.structure.domain.models.HealthStructure

@Composable
fun CarteListeToggle(showMap: Boolean, onShowMap: (Boolean) -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = RepereLavender) {
        Row(Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            ToggleSegment("Carte", showMap) { onShowMap(true) }
            ToggleSegment("Liste", !showMap) { onShowMap(false) }
        }
    }
}

@Composable
private fun ToggleSegment(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) ReperePurple else Color.Transparent)
            .quietClick(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) Color.White else RepereInk,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
fun RepereFilterRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RepereFiltres.forEach { filtre ->
            val active = filtre.label == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (active) ReperePurple else Color.White)
                    .quietClick { onSelect(filtre.label) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    filtre.label,
                    color = if (active) Color.White else RepereInk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
fun RepereQueryField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = RepereMuted,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(placeholder, color = RepereMuted, fontSize = 14.sp)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(color = RepereInk, fontSize = 14.sp),
                    cursorBrush = SolidColor(ReperePurple),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun StructureVisualCard(structure: HealthStructure, onOpen: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().quietClick(onClick = onOpen),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 3.dp,
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StructureCover(
                    structure = structure,
                    contentDescription = structure.displayName,
                    modifier = Modifier.size(74.dp).clip(RoundedCornerShape(16.dp)),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        structure.displayName,
                        color = RepereInk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${structure.structureType ?: "Centre de santé"} · ${structure.displayCity}",
                        color = RepereMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (structure.espaceJeunes()) {
                        Spacer(Modifier.height(4.dp))
                        Text("Espace jeunes", color = RepereTeal, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = ReperePurple,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    structure.displayAddress,
                    color = RepereMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ReperePurple, contentColor = Color.White),
            ) {
                Text("Voir la fiche", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExplorerHeading(showMap: Boolean, onShowMap: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            "Autour de toi",
            color = RepereInk,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            maxLines = 1,
        )
        CarteListeToggle(showMap = showMap, onShowMap = onShowMap)
    }
}

@Composable
fun MapPlacePreview(structure: HealthStructure, onOpen: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().quietClick(onClick = onOpen),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            StructureCover(
                structure = structure,
                contentDescription = structure.displayName,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    structure.displayName,
                    color = RepereInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    structure.structureType ?: "Accueil des jeunes",
                    color = RepereMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                )
                if (structure.espaceJeunes()) {
                    Text("Espace jeunes", color = RepereTeal, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(ReperePurple)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text("Voir la fiche", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
