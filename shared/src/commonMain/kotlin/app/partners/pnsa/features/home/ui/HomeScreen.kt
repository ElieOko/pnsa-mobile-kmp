package app.partners.pnsa.features.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.ui.components.AvatarCircle
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.quietClick
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.navigation.MainTab
import app.partners.pnsa.core.ui.theme.RepereCoral
import app.partners.pnsa.core.ui.theme.RepereInk
import app.partners.pnsa.core.ui.theme.RepereLavender
import app.partners.pnsa.core.ui.theme.RepereMuted
import app.partners.pnsa.core.ui.theme.ReperePurple
import app.partners.pnsa.features.home.data.RepereCatalogue
import app.partners.pnsa.features.home.data.RepereVisuel
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    val user by graph.session.user.collectAsState()
    var zone by remember { mutableStateOf(graph.search.city) }

    fun openExplorer(filtre: String) {
        graph.search.open(filter = filtre, city = zone.trim(), query = "", map = true)
        navigator.openTab(MainTab.Explorer)
    }

    PnsaScaffold(containerColor = Color.White) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RepereWordmark()
                    AvatarCircle(
                        initials = user?.initials?.takeIf { it.isNotBlank() && it != "?" } ?: "RJ",
                        color = ReperePurple,
                    )
                }
            }
            item {
                Text(
                    "Bienvenue sur Repère Jeunes !",
                    color = RepereInk,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    lineHeight = 30.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Le bon service de santé, au bon endroit, près de toi !",
                    color = RepereMuted,
                    fontSize = 15.sp,
                )
            }
            item {
                val hero = RepereCatalogue.hero
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .quietClick { openExplorer(hero.filtre) },
                ) {
                    Image(
                        painter = painterResource(hero.image),
                        contentDescription = hero.titre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(Color(0x99000000))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Column {
                            Text(hero.titre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(hero.detail, color = Color.White.copy(alpha = 0.92f), fontSize = 12.sp, maxLines = 2)
                        }
                    }
                }
            }
            item {
                ZoneSearchField(
                    value = zone,
                    onValueChange = { zone = it },
                )
            }
            item {
                val besoins = RepereCatalogue.besoins
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    besoins.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { visuel ->
                                BesoinCard(
                                    visuel = visuel,
                                    modifier = Modifier.weight(1f),
                                    onClick = { openExplorer(visuel.filtre) },
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = { openExplorer("Tous") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ReperePurple, contentColor = Color.White),
                ) {
                    Text("Trouver un service", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    RepereCatalogue.cartes.forEach { visuel ->
                        CarteRepere(
                            visuel = visuel,
                            onClick = { openExplorer(visuel.filtre) },
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun RepereWordmark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Repère", color = ReperePurple, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        Spacer(Modifier.width(6.dp))
        Text("Jeunes", color = RepereCoral, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
    }
}

@Composable
private fun ZoneSearchField(value: String, onValueChange: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = RepereLavender,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Place, contentDescription = null, tint = ReperePurple, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text("Ville ou zone de santé", color = RepereMuted, fontSize = 14.sp)
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
            Icon(Icons.Default.Search, contentDescription = null, tint = RepereMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun BesoinCard(visuel: RepereVisuel, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(132.dp).quietClick(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = visuel.fond,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(visuel.image),
                contentDescription = visuel.titre,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(62.dp).clip(CircleShape),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                visuel.titre,
                color = RepereInk,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CarteRepere(visuel: RepereVisuel, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().quietClick(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = visuel.fond,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(visuel.image),
                contentDescription = visuel.titre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(visuel.titre, color = RepereInk, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(2.dp))
                Text(visuel.detail, color = RepereMuted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ReperePurple,
            )
        }
    }
}
