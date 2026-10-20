package app.partners.pnsa.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.MainTab
import app.partners.pnsa.core.ui.theme.BadgeColor
import app.partners.pnsa.core.ui.theme.PnsaInk
import app.partners.pnsa.core.ui.theme.WsColor
import app.partners.pnsa.core.ui.theme.WsGreenDark

/**
 * Barre de navigation basse blanche, façon WhatsApp :
 * [NavigationBar] blanche, indicateur [WsColor], icônes 28.dp, labels gras 10.sp.
 * Aucune barre supérieure n'est affichée dans l'application.
 */
@Composable
fun TikTokBottomBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
) {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        MainTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.12f else 1f,
                label = "bottom-tab-icon",
            )
            val (filled, outline) = tabIcons(tab)
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = WsColor,
                    selectedIconColor = WsGreenDark,
                    selectedTextColor = PnsaInk,
                    unselectedIconColor = PnsaInk.copy(alpha = 0.55f),
                    unselectedTextColor = PnsaInk.copy(alpha = 0.55f),
                ),
                onClick = { onSelect(tab) },
                selected = isSelected,
                icon = {
                    Icon(
                        imageVector = if (isSelected) filled else outline,
                        contentDescription = tab.label,
                        modifier = Modifier.size(28.dp).scale(iconScale),
                    )
                },
                label = {
                    Text(tab.label, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                },
            )
        }
    }
}

/** Alias explicite : la bottom bar blanche est le seul chrome permanent. */
@Composable
fun PnsaBottomBar(selected: MainTab, onSelect: (MainTab) -> Unit) = TikTokBottomBar(selected, onSelect)

private fun tabIcons(tab: MainTab): Pair<ImageVector, ImageVector> = when (tab) {
    MainTab.Home -> Icons.Filled.Home to Icons.Outlined.Home
    MainTab.Learn -> Icons.Filled.Favorite to Icons.Outlined.FavoriteBorder
    MainTab.Quiz -> Icons.Filled.Quiz to Icons.Outlined.Quiz
    MainTab.Structures -> Icons.Filled.Map to Icons.Outlined.Map
    MainTab.Forum -> Icons.Filled.Chat to Icons.Outlined.Chat
    MainTab.Profile -> Icons.Filled.Person to Icons.Outlined.Person
}

@Composable
fun PnsaDrawerContent(
    name: String,
    email: String,
    initials: String,
    onDestination: (AppDestination) -> Unit,
    showLogout: Boolean = true,
    onLogout: () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerContentColor = PnsaInk,
        windowInsets = WindowInsets(0, 0, 0, 0),
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(WsGreenDark, BadgeColor)))
                    .padding(20.dp),
            ) {
                Column {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Text(email, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text("Plateforme professionnelle SSR", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                }
            }
            DrawerItem("Accueil", Icons.Default.Home) { onDestination(AppDestination.Home) }
            DrawerItem("Apprendre", Icons.Default.Favorite) { onDestination(AppDestination.Learn) }
            DrawerItem("Quiz", Icons.Default.Quiz) { onDestination(AppDestination.Quizzes) }
            DrawerItem("Carte & structures", Icons.Default.Map) { onDestination(AppDestination.Structures) }
            DrawerItem("Forum", Icons.Default.Chat) { onDestination(AppDestination.Forum) }
            HorizontalDivider(color = PnsaInk.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
            DrawerItem("Conseil privé", Icons.Default.Shield) { onDestination(AppDestination.Advice) }
            DrawerItem("Mes orientations", Icons.Default.Place) { onDestination(AppDestination.Orientations) }
            DrawerItem("Profil", Icons.Default.Person) { onDestination(AppDestination.Profile) }
            DrawerItem("Aide", Icons.Default.Help) { onDestination(AppDestination.Help) }
            DrawerItem("Confidentialité", Icons.Default.Shield) { onDestination(AppDestination.PublicLegal) }
            if (showLogout) {
                HorizontalDivider(color = PnsaInk.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                DrawerItem("Se déconnecter", Icons.Default.Logout, tint = Color(0xFFD01D2A), onClick = onLogout)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    tint: Color = PnsaInk,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .quietClick(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (tint == PnsaInk) BadgeColor else tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = tint, fontWeight = FontWeight.Medium)
    }
}

/**
 * Conserve l'état d'un onglet déjà visité : le contenu reste composé (mais masqué)
 * au lieu d'être détruit — indispensable pour ne pas perdre l'état de la carte
 * (position, marqueur sélectionné, mode de vue) quand on change d'onglet.
 */
@Composable
fun KeepAlivePane(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .graphicsLayer {
                alpha = if (visible) 1f else 0f
                translationX = if (visible) 0f else 4000f
            },
    ) {
        content()
    }
}
