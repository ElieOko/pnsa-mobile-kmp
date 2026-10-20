package app.partners.pnsa.core.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import app.partners.pnsa.core.ui.theme.PnsaGreenDeep
import app.partners.pnsa.core.ui.theme.PnsaInk
import app.partners.pnsa.core.ui.theme.PnsaMuted
import app.partners.pnsa.core.ui.theme.bagdeColor
import app.partners.pnsa.core.ui.theme.wsColor

data class ParentTabItem(
    val id: Int,
    val name: String,
    val tab: MainTab,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

@Composable
fun TikTokTopBar(
    title: String,
    canPop: Boolean,
    onMenu: () -> Unit,
    onBack: () -> Unit,
    initials: String,
    onAvatar: () -> Unit,
) {
    // Topbars removed as part of the WhatsApp-style redesign
}

@Composable
fun TikTokBottomBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
) {
    val listParent = remember {
        MainTab.entries.mapIndexed { index, tab ->
            val (filled, outline) = tabIcons(tab)
            ParentTabItem(
                id = index + 1,
                name = tab.label,
                tab = tab,
                icon = outline,
                selectedIcon = filled,
            )
        }
    }
    val state = remember { mutableIntStateOf(selected.ordinal) }
    LaunchedEffect(selected) {
        state.intValue = selected.ordinal
    }

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
    ) {
        listParent.forEachIndexed { i, parent ->
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = wsColor,
                    selectedIconColor = bagdeColor,
                    selectedTextColor = PnsaInk,
                    unselectedIconColor = PnsaMuted,
                    unselectedTextColor = PnsaMuted,
                ),
                onClick = {
                    state.intValue = i
                    onSelect(parent.tab)
                },
                selected = i == state.intValue,
                icon = {
                    Icon(
                        imageVector = if (i == state.intValue) parent.selectedIcon else parent.icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                },
                label = {
                    Text(
                        parent.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                    )
                },
            )
        }
    }
}

private fun tabIcons(tab: MainTab): Pair<ImageVector, ImageVector> = when (tab) {
    MainTab.Structures -> Icons.Filled.Map to Icons.Outlined.Map
    MainTab.Messages -> Icons.Filled.Sms to Icons.Outlined.Sms
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
                    .background(Brush.linearGradient(listOf(bagdeColor, PnsaGreenDeep)))
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
                    Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(email, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text("Plateforme professionnelle SSR", color = Color.White.copy(alpha = 0.78f), fontSize = 12.sp)
                }
            }
            DrawerItem("Carte & structures", Icons.Default.Map) { onDestination(AppDestination.Structures) }
            DrawerItem("Messagerie", Icons.Default.Sms) { onDestination(AppDestination.Messages) }
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
            DrawerItem("Mes orientations", Icons.Default.Place) { onDestination(AppDestination.Orientations) }
            DrawerItem("Profil", Icons.Default.Person) { onDestination(AppDestination.Profile) }
            DrawerItem("Aide", Icons.Default.Help) { onDestination(AppDestination.Help) }
            DrawerItem("Confidentialité", Icons.Default.Shield) { onDestination(AppDestination.PublicLegal) }
            if (showLogout) {
                HorizontalDivider(color = bagdeColor.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                DrawerItem("Se déconnecter", Icons.Default.Logout, tint = Color(0xFFD32F2F), onClick = onLogout)
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
        Icon(icon, contentDescription = null, tint = if (tint == PnsaInk) bagdeColor else tint, modifier = Modifier.size(22.dp))
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
