package app.partners.pnsa.features.auth.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import app.partners.pnsa.core.config.AppConfig
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.network.ApiException
import app.partners.pnsa.core.ui.components.PnsaTextField
import app.partners.pnsa.core.ui.components.PnsaTopBar
import app.partners.pnsa.core.ui.components.PrimaryAction
import app.partners.pnsa.core.ui.components.QuietAction
import app.partners.pnsa.core.ui.components.SecondaryAction
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.util.DrcLocations
import app.partners.pnsa.features.auth.domain.models.ConsentPayload
import app.partners.pnsa.features.auth.domain.models.RegisterRequest
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onHelp: () -> Unit,
    onLegal: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text("PNSA", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(
                "Santé sexuelle et reproductive, en toute confiance.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Informe-toi, teste tes connaissances, pose une question et trouve une structure adaptée près de chez toi. Tes données restent protégées.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(28.dp))
            PrimaryAction("Se connecter", onClick = onLogin)
            Spacer(Modifier.height(10.dp))
            SecondaryAction("Créer un compte", onClick = onRegister)
            Spacer(Modifier.height(8.dp))
            QuietAction("Aide et limites du service", onClick = onHelp)
            QuietAction("Confidentialité et conditions", onClick = onLegal)
        }
    }
}

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoggedIn: () -> Unit,
    onRegister: () -> Unit,
    onHelp: () -> Unit,
) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var hidePassword by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { PnsaTopBar("Connexion", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Heureux de te revoir", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Connecte-toi pour accéder aux contenus, aux quiz et à l’annuaire SSR.")
            Spacer(Modifier.height(20.dp))
            PnsaTextField(email, { email = it }, "Adresse e-mail")
            Spacer(Modifier.height(12.dp))
            PnsaTextField(
                value = password,
                onValueChange = { password = it },
                label = "Mot de passe",
                visualTransformation = if (hidePassword) PasswordVisualTransformation() else VisualTransformation.None,
                trailing = {
                    IconButton(onClick = { hidePassword = !hidePassword }) {
                        Icon(
                            if (hidePassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Afficher le mot de passe",
                        )
                    }
                },
            )
            error?.let {
                Spacer(Modifier.height(12.dp))
                StatusBanner(it)
            }
            Spacer(Modifier.height(20.dp))
            PrimaryAction(if (loading) "Connexion…" else "Entrer", enabled = !loading) {
                scope.launch {
                    loading = true
                    error = null
                    runCatching { graph.auth.login(email, password) }
                        .onSuccess { onLoggedIn() }
                        .onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
                    loading = false
                }
            }
            QuietAction("Pas encore de compte ? S’inscrire") { onRegister() }
            QuietAction("Mot de passe oublié") {
                scope.launch {
                    snackbar.showSnackbar(
                        "Écris à ${AppConfig.contactEmail} ou appelle ${AppConfig.contactPhone} pour récupérer l’accès.",
                    )
                }
            }
            QuietAction("Besoin d’aide ?") { onHelp() }
        }
    }
}

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegistered: () -> Unit,
    onLegal: () -> Unit,
) {
    val graph = LocalAppGraph.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(0) }
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("F") }
    var dateNaissance by remember { mutableStateOf("2005-01-15") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("Kinshasa") }
    var ville by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }
    var acceptPrivacy by remember { mutableStateOf(false) }
    var statsConsent by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun validate(): String? = when {
        prenom.isBlank() || nom.isBlank() -> "Indique ton prénom et ton nom."
        email.isBlank() || !email.contains("@") -> "Entre une adresse e-mail valide."
        password.length < 8 -> "Le mot de passe doit contenir au moins 8 caractères."
        password != confirm -> "La confirmation du mot de passe ne correspond pas."
        !acceptTerms || !acceptPrivacy -> "Accepte les conditions et la politique de confidentialité pour continuer."
        else -> null
    }

    Scaffold(topBar = { PnsaTopBar("Inscription", onBack = onBack) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                when (step) {
                    0 -> "Qui es-tu ?"
                    1 -> "Ton accès"
                    else -> "Tes choix"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text("Étape ${step + 1} / 3  •  Tes informations restent confidentielles.")
            Spacer(Modifier.height(16.dp))
            when (step) {
                0 -> {
                    PnsaTextField(prenom, { prenom = it }, "Prénom")
                    Spacer(Modifier.height(10.dp))
                    PnsaTextField(nom, { nom = it }, "Nom")
                    Spacer(Modifier.height(12.dp))
                    Text("Genre (volontaire)", fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = genre == "F", onClick = { genre = "F" }, label = { Text("Fille") })
                        FilterChip(selected = genre == "M", onClick = { genre = "M" }, label = { Text("Garçon") })
                    }
                    Spacer(Modifier.height(10.dp))
                    PnsaTextField(dateNaissance, { dateNaissance = it }, "Date de naissance (AAAA-MM-JJ)")
                    Spacer(Modifier.height(10.dp))
                    PnsaTextField(phone, { phone = it }, "Téléphone (optionnel)")
                }
                1 -> {
                    PnsaTextField(email, { email = it }, "Adresse e-mail")
                    Spacer(Modifier.height(10.dp))
                    PnsaTextField(password, { password = it }, "Mot de passe", visualTransformation = PasswordVisualTransformation())
                    Spacer(Modifier.height(10.dp))
                    PnsaTextField(confirm, { confirm = it }, "Confirmer le mot de passe", visualTransformation = PasswordVisualTransformation())
                    Spacer(Modifier.height(10.dp))
                    PnsaTextField(province, { province = it }, "Province")
                    DrcLocations.provinces.take(8).forEach { item ->
                        FilterChip(
                            selected = province == item,
                            onClick = { province = item },
                            label = { Text(item) },
                            modifier = Modifier.padding(end = 4.dp, bottom = 4.dp),
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    PnsaTextField(ville, { ville = it }, "Ville")
                }
                else -> {
                    ConsentRow(
                        checked = acceptPrivacy,
                        onChecked = { acceptPrivacy = it },
                        title = "Politique de confidentialité",
                        body = "J’ai lu et j’accepte la politique de confidentialité (version ${AppConfig.privacyPolicyVersion}).",
                    )
                    ConsentRow(
                        checked = acceptTerms,
                        onChecked = { acceptTerms = it },
                        title = "Conditions d’utilisation",
                        body = "J’accepte les conditions d’utilisation de la plateforme PNSA.",
                    )
                    ConsentRow(
                        checked = statsConsent,
                        onChecked = { statsConsent = it },
                        title = "Statistiques agrégées (optionnel)",
                        body = "Autoriser l’usage de données agrégées pour améliorer le programme. Tu pourras changer d’avis.",
                    )
                    QuietAction("Lire les textes") { onLegal() }
                }
            }
            error?.let {
                Spacer(Modifier.height(12.dp))
                StatusBanner(it)
            }
            Spacer(Modifier.height(20.dp))
            if (step < 2) {
                PrimaryAction("Continuer") { step += 1 }
                if (step > 0) QuietAction("Retour") { step -= 1 }
            } else {
                PrimaryAction(if (loading) "Création…" else "Créer mon compte", enabled = !loading) {
                    val problem = validate()
                    if (problem != null) {
                        error = problem
                        return@PrimaryAction
                    }
                    scope.launch {
                        loading = true
                        error = null
                        val request = RegisterRequest(
                            nom = nom.trim(),
                            prenom = prenom.trim(),
                            email = email.trim(),
                            password = password,
                            passwordConfirmation = confirm,
                            genre = genre,
                            dateNaissance = dateNaissance.trim(),
                            phone = phone.trim().ifBlank { null },
                            province = province.trim().ifBlank { null },
                            ville = ville.trim().ifBlank { null },
                            consents = listOf(
                                ConsentPayload("privacy", acceptPrivacy, AppConfig.privacyPolicyVersion),
                                ConsentPayload("terms", acceptTerms, AppConfig.termsVersion),
                                ConsentPayload("stats", statsConsent, AppConfig.privacyPolicyVersion),
                            ),
                        )
                        runCatching { graph.auth.register(request) }
                            .onSuccess { onRegistered() }
                            .onFailure { error = (it as? ApiException)?.userMessage() ?: it.message }
                        loading = false
                    }
                }
                QuietAction("Étape précédente") { step -= 1 }
            }
        }
    }
}

@Composable
private fun ConsentRow(
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
    title: String,
    body: String,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Column(Modifier.padding(top = 10.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LegalScreen(onBack: () -> Unit) {
    Scaffold(topBar = { PnsaTopBar("Confidentialité", onBack = onBack) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Ce que nous collectons", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Le PNSA collecte les informations de compte (nom, e-mail, éventuellement téléphone, province et ville) pour te permettre d’accéder aux contenus, aux quiz, au forum, au conseil et à l’orientation.")
            Spacer(Modifier.height(12.dp))
            Text("Tes choix", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Les consentements notifications, orientation et statistiques sont gérés séparément. Un refus n’est jamais enregistré comme une acceptation.")
            Spacer(Modifier.height(12.dp))
            Text("Contact", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("${AppConfig.contactEmail}\n${AppConfig.contactPhone}\nKinshasa, RDC")
            Spacer(Modifier.height(12.dp))
            Text("Ce service n’est pas une urgence médicale. En cas de danger, contacte un service de santé ou les personnes de confiance près de toi.")
        }
    }
}
