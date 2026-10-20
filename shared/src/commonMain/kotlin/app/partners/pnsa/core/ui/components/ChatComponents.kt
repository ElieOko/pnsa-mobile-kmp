package app.partners.pnsa.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.ui.theme.BadgeColor
import app.partners.pnsa.core.ui.theme.PnsaInk
import app.partners.pnsa.core.ui.theme.WsBubbleSent
import app.partners.pnsa.core.ui.theme.WsColor
import app.partners.pnsa.core.ui.theme.WsGreenDark

/** Avatar rond vert avec initiale, façon liste de discussions WhatsApp. */
@Composable
fun DiscussionAvatar(name: String, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 52.dp, selected: Boolean = false) {
    Box(modifier.size(size), contentAlignment = Alignment.BottomEnd) {
        Box(
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(WsGreenDark, BadgeColor))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                name.trim().firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.4f).sp,
            )
        }
        if (selected) {
            Box(
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(BadgeColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, contentDescription = "Sélectionné", tint = Color.White, modifier = Modifier.size(13.dp))
            }
        }
    }
}

/** Pastille verte du nombre de messages non lus. */
@Composable
fun UnreadBadge(count: Int, modifier: Modifier = Modifier) {
    if (count <= 0) return
    Box(
        modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(BadgeColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (count > 99) "99+" else count.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Bulle de conversation : envoi aligné à droite (vert clair, double coche),
 * réponse alignée à gauche (blanche, nom de l'auteur en vert).
 */
@Composable
fun ChatBubble(
    text: String,
    mine: Boolean,
    modifier: Modifier = Modifier,
    author: String? = null,
    time: String? = null,
    read: Boolean? = null,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (mine) 18.dp else 4.dp,
                bottomEnd = if (mine) 4.dp else 18.dp,
            ),
            color = if (mine) WsBubbleSent else Color.White,
            shadowElevation = if (mine) 1.dp else 2.dp,
            modifier = Modifier.widthIn(max = 292.dp),
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!mine && !author.isNullOrBlank()) {
                    Text(author, color = WsGreenDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(Modifier.size(2.dp))
                }
                Text(text, color = PnsaInk, fontSize = 15.sp, lineHeight = 20.sp)
                Row(
                    Modifier.align(Alignment.End).padding(top = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    if (!time.isNullOrBlank()) {
                        Text(time, fontSize = 10.sp, color = PnsaInk.copy(alpha = 0.55f))
                    }
                    if (mine && read != null) {
                        Icon(
                            if (read) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = if (read) "Lu" else "Envoyé",
                            tint = if (read) BadgeColor else PnsaInk.copy(alpha = 0.45f),
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Zone de saisie ronde façon WhatsApp : pilule blanche + bouton vert
 * (micro si vide, envoi animé sinon). Aucune autre zone de saisie sur la carte.
 */
@Composable
fun MessageComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Message",
    sending: Boolean = false,
) {
    val canSend = value.isNotBlank() && !sending
    val buttonColor by animateColorAsState(if (canSend) BadgeColor else BadgeColor.copy(alpha = 0.85f), label = "composer-button")
    val buttonScale by animateFloatAsState(if (canSend) 1f else 0.92f, label = "composer-scale")
    Row(
        modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.weight(1f),
        ) {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
                if (value.isEmpty()) {
                    Text(placeholder, color = PnsaInk.copy(alpha = 0.45f), fontSize = 15.sp)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(color = PnsaInk, fontSize = 15.sp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 110.dp),
                )
            }
        }
        Box(
            Modifier
                .size(50.dp)
                .scale(buttonScale)
                .clip(CircleShape)
                .background(buttonColor)
                .quietClick(enabled = canSend, onClick = onSend),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (value.isBlank()) Icons.Default.Mic else Icons.AutoMirrored.Filled.Send,
                contentDescription = if (value.isBlank()) "Message vocal" else "Envoyer",
                tint = Color.White,
                modifier = Modifier.size(23.dp),
            )
        }
    }
}

/** Barre flottante de sélection multiple (appui long sur une discussion). */
@Composable
fun SelectionActionBar(
    count: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleFavorite: (() -> Unit)? = null,
) {
    Surface(
        modifier.fillMaxWidth(),
        color = BadgeColor,
        shadowElevation = 6.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).quietClick(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Close, contentDescription = "Fermer la sélection", tint = Color.White)
            }
            Text("$count sélectionné${if (count > 1) "s" else ""}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            if (onToggleFavorite != null) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).quietClick(onClick = onToggleFavorite),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Ajouter aux favoris",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}
