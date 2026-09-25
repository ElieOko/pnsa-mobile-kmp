package app.partners.pnsa.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.MainTab
import app.partners.pnsa.core.ui.theme.PnsaBlue
import app.partners.pnsa.core.ui.theme.PnsaChrome
import app.partners.pnsa.core.ui.theme.PnsaChromeLift
import app.partners.pnsa.core.ui.theme.PnsaRed
import app.partners.pnsa.core.ui.theme.PnsaRedHot

@Composable
fun TikTokTopBar(
    title: String,
    canPop: Boolean,
    onMenu: () -> Unit,
    onBack: () -> Unit,
    initials: String,
    onAvatar: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PnsaChrome)
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(48.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { if (canPop) onBack() else onMenu() }) {
            Icon(
                if (canPop) Icons.Default.ArrowBack else Icons.Default.Menu,
                contentDescription = if (canPop) "Retour" else "Menu",
                tint = Color.White,
            )
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
                Box(
                    Modifier
                        .padding(top = 2.dp)
                        .width(28.dp)
                        .height(2.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(PnsaBlue, PnsaRed))),
                )
            }
        }
        Box(
            Modifier
                .padding(end = 6.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(PnsaBlue, PnsaRed)))
                .quietClick(onClick = onAvatar),
            contentAlignment = Alignment.Center,
        ) {
            Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun TikTokBottomBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PnsaChrome)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(52.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        MainTab.entries.forEach { tab ->
            TikTokTabItem(
                tab = tab,
                selected = selected == tab,
                onClick = { onSelect(tab) },
            )
        }
    }
}

@Composable
private fun RowScope.TikTokTabItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (selected) PnsaRedHot else Color.White.copy(alpha = 0.55f)
    val (filled, outline) = tabIcons(tab)
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .quietClick(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) filled else outline,
            contentDescription = tab.label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            tab.label,
            color = tint,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

private fun tabIcons(tab: MainTab): Pair<ImageVector, ImageVector> = when (tab) {
    MainTab.Home -> Icons.Filled.Home to Icons.Outlined.Home
    MainTab.Learn -> Icons.Filled.Favorite to Icons.Outlined.FavoriteBorder
    MainTab.Quiz -> Icons.Filled.Quiz to Icons.Outlined.Quiz
    MainTab.Structures -> Icons.Filled.Map to Icons.Outlined.Map
    MainTab.Forum -> Icons.Filled.Chat to Icons.Outlined.Chat
}

@Composable
fun PnsaDrawerContent(
    name: String,
    email: String,
    initials: String,
    onDestination: (AppDestination) -> Unit,
    onLogout: () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = PnsaChromeLift,
        drawerContentColor = Color.White,
        windowInsets = WindowInsets(0, 0, 0, 0),
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF0B3C8A), PnsaRed)))
                    .padding(20.dp),
            ) {
                Column {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(email, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    Text("Plateforme professionnelle SSR", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
            DrawerItem("Accueil", Icons.Default.Home) { onDestination(AppDestination.Home) }
            DrawerItem("Apprendre", Icons.Default.Favorite) { onDestination(AppDestination.Learn) }
            DrawerItem("Quiz", Icons.Default.Quiz) { onDestination(AppDestination.Quizzes) }
            DrawerItem("Carte & structures", Icons.Default.Map) { onDestination(AppDestination.Structures) }
            DrawerItem("Forum", Icons.Default.Chat) { onDestination(AppDestination.Forum) }
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
            DrawerItem("Conseil privé", Icons.Default.Shield) { onDestination(AppDestination.Advice) }
            DrawerItem("Mes orientations", Icons.Default.Place) { onDestination(AppDestination.Orientations) }
            DrawerItem("Profil", Icons.Default.Person) { onDestination(AppDestination.Profile) }
            DrawerItem("Aide", Icons.Default.Help) { onDestination(AppDestination.Help) }
            DrawerItem("Confidentialité", Icons.Default.Shield) { onDestination(AppDestination.PublicLegal) }
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
            DrawerItem("Se déconnecter", Icons.Default.Logout, tint = PnsaRedHot, onClick = onLogout)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    tint: Color = Color.White,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .quietClick(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = tint, fontWeight = FontWeight.Medium)
    }
}

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
