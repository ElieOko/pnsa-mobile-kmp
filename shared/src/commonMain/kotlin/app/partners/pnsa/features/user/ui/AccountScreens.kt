package app.partners.pnsa.features.user.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.config.AppConfig
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.network.ApiException
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.InlineBackHeader
import app.partners.pnsa.core.ui.components.LoadingState
import app.partners.pnsa.core.ui.components.PnsaCard
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.PnsaTextField
import app.partners.pnsa.core.ui.components.PrimaryAction
import app.partners.pnsa.core.ui.components.SecondaryAction
import app.partners.pnsa.core.ui.components.ShortcutTile
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.components.quietClick
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.theme.PnsaGreenSoft
import app.partners.pnsa.core.ui.theme.PnsaInk
import app.partners.pnsa.core.ui.theme.PnsaMuted
import app.partners.pnsa.core.ui.theme.bagdeColor
import app.partners.pnsa.core.ui.theme.wsColor
import app.partners.pnsa.features.auth.domain.models.ConsentPayload
import app.partners.pnsa.features.auth.domain.models.NotificationPreferencesUpdate
import app.partners.pnsa.features.auth.domain.models.ProfileUpdateRequest
import app.partners.pnsa.features.content.domain.models.Faq
import app.partners.pnsa.features.user.domain.models.NotificationPreferences
import app.partners.pnsa.features.user.domain.models.ProfileBundle
import kotlinx.coroutines.launch

@Composable
fun MoreMenuScreen(navigator: AppNavigator) {
    PnsaScaffold { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InlineBackHeader(
                title = "Plus d’options",
                subtitle = "Accès rapide aux services PNSA",
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShortcutTile("Carte", "Trouver une structure", Icons.Default.Map, { navigator.push(AppDestination.Structures) }, Modifier.weight(1f))
                ShortcutTile("Messagerie", "Chat avec une structure", Icons.Default.Chat, { navigator.push(AppDestination.Messages) }, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShortcutTile("Orientations", "Mes références", Icons.Default.Place, { navigator.push(AppDestination.Orientations) }, Modifier.weight(1f))
                ShortcutTile("Profil", "Compte et choix", Icons.Default.Edit, { navigator.push(AppDestination.Profile) }, Modifier.weight(1f))
            }
            ShortcutTile("Aide", "FAQ, contact, limites", Icons.Default.Help, { navigator.push(AppDestination.Help) })
            StatusBanner("Ce service informe et oriente. Il ne remplace pas une consultation ni une urgence médicale.")
        }
    }
}

@Composable
fun ProfileScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    val user by graph.session.user.collectAsState()
    val scope = rememberCoroutineScope()
    var bundle by remember { mutableStateOf<ProfileBundle?>(graph.screens.profile) }
    var loading by remember { mutableStateOf(!graph.screens.profileLoaded) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!graph.screens.profileLoaded) {
            runCatching { graph.profile.bundle() }
                .onSuccess {
                    bundle = it
                    graph.screens.profile = it
                    graph.screens.profileLoaded = true
                }
            loading = false
        }
    }

    val displayName = user?.displayName?.ifBlank { null } ?: "Professionnel PNSA"
    val displayEmail = user?.email?.ifBlank { null } ?: "contact@pnsa-rdc.org"
    val displayPhone = user?.phone?.ifBlank { null } ?: AppConfig.contactPhone
    val displayLocation = listOfNotNull(user?.province, user?.ville).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "Kinshasa · RDC" }
    val displayRole = user?.typeUser?.libelle ?: "Membre certifié SSR"
    val initials = user?.initials?.takeIf { it != "?" } ?: "PN"

    PnsaScaffold { padding ->
        if (loading) {
            LoadingState()
            return@PnsaScaffold
        }
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            InlineBackHeader(
                title = "Mon Profil",
                subtitle = "Identité professionnelle, préférences et confidentialité",
                onBack = if (navigator.canPop) ({ navigator.pop() }) else null,
                trailing = {
                    Surface(
                        modifier = Modifier.quietClick { navigator.push(AppDestination.ProfileEdit) },
                        shape = RoundedCornerShape(20.dp),
                        color = wsColor,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = bagdeColor, modifier = Modifier.size(16.dp))
                            Text("Modifier", color = PnsaInk, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                },
            )

            // Hero Profile Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.5.dp, bagdeColor.copy(alpha = 0.28f)),
                shadowElevation = 3.dp,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(wsColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = initials,
                                color = bagdeColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(bagdeColor)
                                .quietClick { navigator.push(AppDestination.ProfileEdit) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = PnsaInk)
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = CircleShape, color = wsColor) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = bagdeColor, modifier = Modifier.size(14.dp))
                            Text(displayRole, color = PnsaInk, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = bagdeColor.copy(alpha = 0.14f))
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        ProfileStatBadge(label = "Province", value = user?.province?.ifBlank { null } ?: "Kinshasa")
                        ProfileStatBadge(label = "Langue", value = (user?.langue ?: "FR").uppercase())
                        ProfileStatBadge(label = "Statut", value = "Actif")
                    }
                }
            }

            // Personal Info Section
            PnsaCard {
                Text("Coordonnées & Informations", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = bagdeColor)
                Spacer(Modifier.height(12.dp))
                ProfileInfoRow(icon = Icons.Default.Person, label = "Nom complet", value = displayName)
                HorizontalDivider(color = Color(0xFFF0F5F0), modifier = Modifier.padding(vertical = 8.dp))
                ProfileInfoRow(icon = Icons.Default.Email, label = "Adresse e-mail", value = displayEmail)
                HorizontalDivider(color = Color(0xFFF0F5F0), modifier = Modifier.padding(vertical = 8.dp))
                ProfileInfoRow(icon = Icons.Default.Call, label = "Téléphone", value = displayPhone)
                HorizontalDivider(color = Color(0xFFF0F5F0), modifier = Modifier.padding(vertical = 8.dp))
                ProfileInfoRow(icon = Icons.Default.LocationCity, label = "Localisation", value = displayLocation)
            }

            // Quick Services Navigation
            PnsaCard {
                Text("Espace professionnel & Services", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = bagdeColor)
                Spacer(Modifier.height(8.dp))
                ProfileActionRow(
                    icon = Icons.Default.Place,
                    title = "Mes orientations SSR",
                    subtitle = "Consulter et suivre mes références de structures",
                    onClick = { navigator.push(AppDestination.Orientations) },
                )
                HorizontalDivider(color = Color(0xFFF0F5F0), modifier = Modifier.padding(vertical = 6.dp))
                ProfileActionRow(
                    icon = Icons.Default.Help,
                    title = "Aide & Questions fréquentes",
                    subtitle = "Guide d’utilisation et contacts officiels PNSA",
                    onClick = { navigator.push(AppDestination.Help) },
                )
                HorizontalDivider(color = Color(0xFFF0F5F0), modifier = Modifier.padding(vertical = 6.dp))
                ProfileActionRow(
                    icon = Icons.Default.Badge,
                    title = "Confidentialité & Mentions légales",
                    subtitle = "Politique de protection des données (v${AppConfig.privacyPolicyVersion})",
                    onClick = { navigator.push(AppDestination.PublicLegal) },
                )
            }

            // Notification Preferences
            val prefs = bundle?.notificationPreferences ?: NotificationPreferences()
            PnsaCard {
                NotificationBlock(prefs) { updated ->
                    scope.launch {
                        runCatching { graph.profile.updateNotifications(updated) }
                            .onSuccess { message = "Préférences de notifications enregistrées." }
                            .onFailure { message = "Préférences mises à jour localement." }
                    }
                }
            }

            // Consents
            if (!bundle?.consents.isNullOrEmpty()) {
                PnsaCard {
                    Text("Mes consentements", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = bagdeColor)
                    Spacer(Modifier.height(8.dp))
                    bundle?.consents?.forEach { consent ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(consent.consentType ?: "Consentement", fontWeight = FontWeight.Medium, color = PnsaInk)
                            Surface(shape = CircleShape, color = if (consent.granted) wsColor else Color(0xFFF2F2F2)) {
                                Text(
                                    text = if (consent.granted) "Accepté" else "Refusé",
                                    color = if (consent.granted) bagdeColor else PnsaMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
            }

            message?.let { StatusBanner(it) }

            PrimaryAction("Modifier mes informations") {
                navigator.push(AppDestination.ProfileEdit)
            }

            SecondaryAction("Demander la suppression du compte") {
                message = "Demande reçue côté application. Écris à ${AppConfig.contactEmail} pour le traitement institutionnel."
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileStatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = bagdeColor)
        Text(label, fontSize = 11.sp, color = PnsaMuted)
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PnsaGreenSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = bagdeColor, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = PnsaMuted)
            Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = PnsaInk)
        }
    }
}

@Composable
private fun ProfileActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .quietClick(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(wsColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = bagdeColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = PnsaInk)
            Text(subtitle, fontSize = 12.sp, color = PnsaMuted)
        }
        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = bagdeColor, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun NotificationBlock(
    prefs: NotificationPreferences,
    onSave: (NotificationPreferencesUpdate) -> Unit,
) {
    var push by remember(prefs) { mutableStateOf(prefs.pushEnabled) }
    var forum by remember(prefs) { mutableStateOf(prefs.forumEnabled) }
    var advice by remember(prefs) { mutableStateOf(prefs.adviceEnabled) }
    var orientation by remember(prefs) { mutableStateOf(prefs.orientationEnabled) }

    Text("Préférences de notifications", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = bagdeColor)
    Spacer(Modifier.height(6.dp))
    PrefSwitch("Notifications générales", push) {
        push = it
        onSave(NotificationPreferencesUpdate(pushEnabled = it, forumEnabled = forum, adviceEnabled = advice, orientationEnabled = orientation))
    }
    PrefSwitch("Discussions", forum) {
        forum = it
        onSave(NotificationPreferencesUpdate(pushEnabled = push, forumEnabled = it, adviceEnabled = advice, orientationEnabled = orientation))
    }
    PrefSwitch("Suivi des orientations", orientation) {
        orientation = it
        onSave(NotificationPreferencesUpdate(pushEnabled = push, forumEnabled = forum, adviceEnabled = advice, orientationEnabled = it))
    }
}

@Composable
private fun PrefSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, modifier = Modifier.weight(1f), color = PnsaInk, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = bagdeColor,
                uncheckedThumbColor = PnsaMuted,
                uncheckedTrackColor = PnsaGreenSoft,
            ),
        )
    }
}

@Composable
fun ProfileEditScreen(onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    val user by graph.session.user.collectAsState()
    val scope = rememberCoroutineScope()
    var nom by remember(user) { mutableStateOf(user?.nom.orEmpty()) }
    var prenom by remember(user) { mutableStateOf(user?.prenom.orEmpty()) }
    var phone by remember(user) { mutableStateOf(user?.phone.orEmpty()) }
    var province by remember(user) { mutableStateOf(user?.province.orEmpty()) }
    var ville by remember(user) { mutableStateOf(user?.ville.orEmpty()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    PnsaScaffold { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            InlineBackHeader(
                title = "Modifier le profil",
                subtitle = "Mets à jour tes coordonnées professionnelles",
                onBack = onBack,
            )
            Spacer(Modifier.height(12.dp))
            PnsaCard {
                PnsaTextField(prenom, { prenom = it }, "Prénom")
                Spacer(Modifier.height(10.dp))
                PnsaTextField(nom, { nom = it }, "Nom")
                Spacer(Modifier.height(10.dp))
                PnsaTextField(phone, { phone = it }, "Téléphone")
                Spacer(Modifier.height(10.dp))
                PnsaTextField(province, { province = it }, "Province")
                Spacer(Modifier.height(10.dp))
                PnsaTextField(ville, { ville = it }, "Ville / Commune")
            }
            error?.let {
                Spacer(Modifier.height(12.dp))
                StatusBanner(it)
            }
            Spacer(Modifier.height(16.dp))
            PrimaryAction(if (loading) "Enregistrement…" else "Enregistrer les modifications", enabled = !loading) {
                scope.launch {
                    loading = true
                    runCatching {
                        graph.profile.update(
                            ProfileUpdateRequest(
                                nom = nom,
                                prenom = prenom,
                                phone = phone,
                                province = province,
                                ville = ville,
                                langue = "fr",
                            ),
                        )
                    }.onSuccess { onBack() }
                        .onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
                    loading = false
                }
            }
            Spacer(Modifier.height(10.dp))
            SecondaryAction("Mettre à jour mes consentements") {
                scope.launch {
                    runCatching {
                        graph.profile.updateConsents(
                            listOf(
                                ConsentPayload("privacy", true, AppConfig.privacyPolicyVersion),
                                ConsentPayload("terms", true, AppConfig.termsVersion),
                            ),
                        )
                    }.onSuccess { error = "Consentements mis à jour." }
                        .onFailure { error = it.message }
                }
            }
        }
    }
}

@Composable
fun HelpScreen(onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    var faqs by remember { mutableStateOf<List<Faq>>(emptyList()) }

    LaunchedEffect(Unit) {
        faqs = graph.catalog.cachedFaqs().ifEmpty {
            runCatching { graph.catalog.home().faqs }.getOrDefault(emptyList())
        }
    }

    PnsaScaffold { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize().background(Color.White).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                InlineBackHeader(
                    title = "Aide & Support",
                    subtitle = "Mode d’emploi et réponses officielles PNSA",
                    onBack = onBack,
                )
                Text(
                    "Cherche une structure sur la carte, échange directement avec une structure dans les discussions, et crée une orientation si besoin.",
                    color = PnsaMuted,
                )
            }
            item {
                PnsaCard {
                    Text("Contact officiel PNSA", fontWeight = FontWeight.Bold, color = bagdeColor)
                    Spacer(Modifier.height(6.dp))
                    Text(AppConfig.contactEmail, fontWeight = FontWeight.Medium, color = PnsaInk)
                    Text(AppConfig.contactPhone, color = PnsaInk)
                    Text("Kinshasa, République Démocratique du Congo", color = PnsaMuted)
                }
            }
            item {
                StatusBanner("Ouvrir un lien e-mail ou téléphone n’est pas une preuve que ta demande a été reçue par le PNSA.")
            }
            items(faqs, key = { it.id ?: it.libelle.orEmpty() }) { faq ->
                PnsaCard {
                    Text(faq.libelle.orEmpty(), fontWeight = FontWeight.Bold, color = PnsaInk)
                    Spacer(Modifier.height(4.dp))
                    Text(faq.description.orEmpty(), color = PnsaMuted)
                }
            }
            if (faqs.isEmpty()) {
                item {
                    EmptyState("FAQ indisponible hors ligne", "Reconnecte-toi pour charger les réponses officielles.")
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
