package app.partners.pnsa.features.user.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.partners.pnsa.core.config.AppConfig
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.network.ApiException
import app.partners.pnsa.core.ui.components.AvatarCircle
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.ErrorState
import app.partners.pnsa.core.ui.components.LoadingState
import app.partners.pnsa.core.ui.components.PnsaCard
import app.partners.pnsa.core.ui.components.PnsaTextField
import app.partners.pnsa.core.ui.components.PnsaTopBar
import app.partners.pnsa.core.ui.components.PrimaryAction
import app.partners.pnsa.core.ui.components.QuietAction
import app.partners.pnsa.core.ui.components.SecondaryAction
import app.partners.pnsa.core.ui.components.ShortcutTile
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.features.auth.domain.models.ConsentPayload
import app.partners.pnsa.features.auth.domain.models.NotificationPreferencesUpdate
import app.partners.pnsa.features.auth.domain.models.ProfileUpdateRequest
import app.partners.pnsa.features.content.domain.models.Faq
import app.partners.pnsa.features.user.domain.models.NotificationPreferences
import app.partners.pnsa.features.user.domain.models.ProfileBundle
import kotlinx.coroutines.launch

@Composable
fun MoreMenuScreen(navigator: AppNavigator) {
    Scaffold(topBar = { PnsaTopBar("Plus") }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShortcutTile("Forum", "Questions publiques", Icons.Default.Help, { navigator.push(AppDestination.Forum) }, Modifier.weight(1f))
                ShortcutTile("Conseil", "Espace privé", Icons.Default.Notifications, { navigator.push(AppDestination.Advice) }, Modifier.weight(1f))
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
fun ProfileScreen(navigator: AppNavigator, onLoggedOut: () -> Unit) {
    val graph = LocalAppGraph.current
    val user by graph.session.user.collectAsState()
    val scope = rememberCoroutineScope()
    var bundle by remember { mutableStateOf<ProfileBundle?>(null) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching { graph.profile.bundle() }
            .onSuccess { bundle = it }
        loading = false
    }

    Scaffold(topBar = { PnsaTopBar("Mon profil", onBack = { navigator.pop() }) }) { padding ->
        if (loading) {
            LoadingState()
            return@Scaffold
        }
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            AvatarCircle(user?.initials ?: "?")
            Spacer(Modifier.height(12.dp))
            Text(user?.displayName ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(user?.email ?: "")
            Text(listOfNotNull(user?.province, user?.ville).joinToString(" · "))
            Spacer(Modifier.height(16.dp))
            PrimaryAction("Modifier le profil") { navigator.push(AppDestination.ProfileEdit) }
            Spacer(Modifier.height(10.dp))
            bundle?.consents?.forEach { consent ->
                PnsaCard(Modifier.padding(bottom = 8.dp)) {
                    Text(consent.consentType ?: "Consentement", fontWeight = FontWeight.SemiBold)
                    Text(if (consent.granted) "Accepté" else "Refusé")
                }
            }
            bundle?.notificationPreferences?.let { prefs ->
                NotificationBlock(prefs) { updated ->
                    scope.launch {
                        runCatching { graph.profile.updateNotifications(updated) }
                            .onSuccess { message = "Préférences enregistrées." }
                            .onFailure { message = (it as? ApiException)?.userMessage() ?: it.message }
                    }
                }
            }
            message?.let { StatusBanner(it) }
            Spacer(Modifier.height(16.dp))
            SecondaryAction("Se déconnecter") {
                scope.launch {
                    graph.auth.logout()
                    graph.cache.clear()
                    graph.screens.clear()
                    onLoggedOut()
                }
            }
            QuietAction("Demander la suppression du compte") {
                message = "Demande reçue côté application. Écris à ${AppConfig.contactEmail} pour le traitement institutionnel. Nous n’afficherons pas « tout a été supprimé » tant que la procédure n’est pas confirmée."
            }
        }
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

    Text("Notifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    PrefSwitch("Notifications générales", push) {
        push = it
        onSave(NotificationPreferencesUpdate(pushEnabled = it, forumEnabled = forum, adviceEnabled = advice, orientationEnabled = orientation))
    }
    PrefSwitch("Forum", forum) {
        forum = it
        onSave(NotificationPreferencesUpdate(pushEnabled = push, forumEnabled = it, adviceEnabled = advice, orientationEnabled = orientation))
    }
    PrefSwitch("Conseil privé", advice) {
        advice = it
        onSave(NotificationPreferencesUpdate(pushEnabled = push, forumEnabled = forum, adviceEnabled = it, orientationEnabled = orientation))
    }
    PrefSwitch("Orientations", orientation) {
        orientation = it
        onSave(NotificationPreferencesUpdate(pushEnabled = push, forumEnabled = forum, adviceEnabled = advice, orientationEnabled = it))
    }
}

@Composable
private fun PrefSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChecked)
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

    Scaffold(topBar = { PnsaTopBar("Modifier le profil", onBack = onBack) }) { padding ->
        Column(Modifier.padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
            PnsaTextField(prenom, { prenom = it }, "Prénom")
            Spacer(Modifier.height(8.dp))
            PnsaTextField(nom, { nom = it }, "Nom")
            Spacer(Modifier.height(8.dp))
            PnsaTextField(phone, { phone = it }, "Téléphone")
            Spacer(Modifier.height(8.dp))
            PnsaTextField(province, { province = it }, "Province")
            Spacer(Modifier.height(8.dp))
            PnsaTextField(ville, { ville = it }, "Ville")
            error?.let {
                Spacer(Modifier.height(12.dp))
                StatusBanner(it)
            }
            Spacer(Modifier.height(16.dp))
            PrimaryAction(if (loading) "Enregistrement…" else "Enregistrer", enabled = !loading) {
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
            Spacer(Modifier.height(12.dp))
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

    Scaffold(topBar = { PnsaTopBar("Aide", onBack = onBack) }) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Mode d’emploi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Explore Apprendre, teste un quiz, cherche une structure, puis crée une orientation si besoin. Le forum et le conseil privé sont séparés.")
            }
            item {
                PnsaCard {
                    Text("Contact PNSA", fontWeight = FontWeight.SemiBold)
                    Text(AppConfig.contactEmail)
                    Text(AppConfig.contactPhone)
                    Text("Kinshasa, RDC")
                }
            }
            item {
                StatusBanner("Ouvrir un lien e-mail ou téléphone n’est pas une preuve que ta demande a été reçue par le PNSA.")
            }
            items(faqs, key = { it.id ?: it.libelle.orEmpty() }) { faq ->
                PnsaCard {
                    Text(faq.libelle.orEmpty(), fontWeight = FontWeight.SemiBold)
                    Text(faq.description.orEmpty())
                }
            }
            if (faqs.isEmpty()) {
                item {
                    EmptyState("FAQ indisponible hors ligne", "Reconnecte-toi pour charger les réponses officielles.")
                }
            }
        }
    }
}
