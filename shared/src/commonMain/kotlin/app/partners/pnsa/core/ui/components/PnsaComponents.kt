package app.partners.pnsa.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.ui.theme.PnsaGreenSoft
import app.partners.pnsa.core.ui.theme.PnsaInk
import app.partners.pnsa.core.ui.theme.PnsaMuted
import app.partners.pnsa.core.ui.theme.bagdeColor
import app.partners.pnsa.core.ui.theme.wsColor

@Composable
fun PnsaScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    containerColor: Color = Color.White,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        snackbarHost = snackbarHost,
        containerColor = containerColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        content = content,
    )
}

@Composable
fun PnsaTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    // TopAppBar suppressed globally in favor of inline headers and WhatsApp-style layout
}

@Composable
fun InlineBackHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Surface(
                modifier = Modifier
                    .size(38.dp)
                    .quietClick(onClick = onBack),
                shape = CircleShape,
                color = wsColor,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = bagdeColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = bagdeColor,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = PnsaMuted,
                    fontSize = 12.sp,
                )
            }
        }
        trailing?.invoke()
    }
}

fun Modifier.quietClick(enabled: Boolean = true, onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(
        enabled = enabled,
        indication = null,
        interactionSource = null,
        onClick = onClick,
    ),
)

@Composable
fun PageBackdrop(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { content() }
}

@Composable
fun PnsaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.quietClick(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, bagdeColor.copy(alpha = 0.16f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        content = { Column(Modifier.padding(16.dp), content = content) },
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = PnsaInk,
        modifier = modifier.padding(vertical = 8.dp),
    )
}

@Composable
fun PnsaChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    SuggestionChip(
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(35.dp),
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
        },
        onClick = onClick,
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = enabled,
            borderColor = if (selected) bagdeColor else Color(0xFFD5E4D6),
        ),
        colors = if (selected) {
            SuggestionChipDefaults.suggestionChipColors(
                containerColor = wsColor,
                labelColor = PnsaInk,
                disabledContainerColor = wsColor,
                disabledLabelColor = wsColor,
            )
        } else {
            SuggestionChipDefaults.suggestionChipColors(
                containerColor = Color.White,
                labelColor = PnsaMuted,
            )
        },
    )
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = wsColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = PnsaInk,
        )
    }
}

@Composable
fun StatusBanner(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = PnsaGreenSoft,
        border = BorderStroke(1.dp, bagdeColor.copy(alpha = 0.25f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = PnsaInk,
        )
    }
}

@Composable
fun LoadingState(label: String = "Chargement…") {
    Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = bagdeColor)
            Spacer(Modifier.height(12.dp))
            Text(label, color = PnsaMuted)
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    icon: ImageVector = Icons.Default.Info,
) {
    FeedbackState(icon, title, message, actionLabel, onAction)
}

@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    offline: Boolean = false,
) {
    FeedbackState(
        icon = Icons.Default.Warning,
        title = if (offline) "Pas de connexion" else "Impossible de continuer",
        message = message,
        actionLabel = if (onRetry != null) "Réessayer" else null,
        onAction = onRetry,
    )
}

@Composable
private fun FeedbackState(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(wsColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = bagdeColor)
            }
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PnsaInk)
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = PnsaMuted,
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = bagdeColor, contentColor = Color.White),
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun PnsaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    supportingText: String? = null,
    isError: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        trailingIcon = trailing,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PnsaInk,
            unfocusedTextColor = PnsaInk,
            disabledTextColor = PnsaMuted,
            cursorColor = bagdeColor,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedBorderColor = bagdeColor,
            unfocusedBorderColor = Color(0xFFD5E4D6),
            focusedLabelColor = bagdeColor,
            unfocusedLabelColor = PnsaMuted,
        ),
    )
}

@Composable
fun ShortcutTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.quietClick(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, bagdeColor.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(wsColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = bagdeColor)
            }
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.SemiBold, color = PnsaInk)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = PnsaMuted,
                maxLines = 2,
            )
        }
    }
}

@Composable
fun ScreenPadding(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        content = content,
    )
}

@Composable
fun PrimaryAction(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bagdeColor,
            contentColor = Color.White,
            disabledContainerColor = bagdeColor.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.8f),
        ),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryAction(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = wsColor,
            contentColor = PnsaInk,
        ),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun QuietAction(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text, color = bagdeColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AvatarCircle(initials: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bagdeColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MetaRow(items: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.filter { it.isNotBlank() }.forEach { Pill(it) }
    }
}
