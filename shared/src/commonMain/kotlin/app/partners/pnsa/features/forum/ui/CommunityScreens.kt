package app.partners.pnsa.features.forum.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import app.partners.pnsa.core.audio.PlatformVoicePlayer
import app.partners.pnsa.core.audio.PlatformVoiceRecorder
import app.partners.pnsa.core.audio.VoiceMessageCodec
import app.partners.pnsa.core.audio.VoicePayload
import app.partners.pnsa.core.audio.formatVoiceTimer
import app.partners.pnsa.core.audio.rememberEnsureAudioPermission
import app.partners.pnsa.core.config.AppConfig
import app.partners.pnsa.core.di.LocalAppGraph
import app.partners.pnsa.core.ui.components.EmptyState
import app.partners.pnsa.core.ui.components.LoadingState
import app.partners.pnsa.core.ui.components.PageBackdrop
import app.partners.pnsa.core.ui.components.PnsaScaffold
import app.partners.pnsa.core.ui.components.StatusBanner
import app.partners.pnsa.core.ui.components.quietClick
import app.partners.pnsa.core.ui.navigation.AppDestination
import app.partners.pnsa.core.ui.navigation.AppNavigator
import app.partners.pnsa.core.ui.theme.PnsaChatCanvas
import app.partners.pnsa.core.ui.theme.PnsaGreenBubbleSent
import app.partners.pnsa.core.ui.theme.PnsaGreenSoft
import app.partners.pnsa.core.ui.theme.PnsaInk
import app.partners.pnsa.core.ui.theme.PnsaMuted
import app.partners.pnsa.core.ui.theme.PnsaPink
import app.partners.pnsa.core.ui.theme.bagdeColor
import app.partners.pnsa.core.ui.theme.wsColor
import app.partners.pnsa.core.util.excerpt
import app.partners.pnsa.core.util.formatIsoDate
import app.partners.pnsa.features.forum.domain.models.Categorie
import app.partners.pnsa.features.forum.domain.models.Discussion
import app.partners.pnsa.features.forum.domain.models.Message
import app.partners.pnsa.features.forum.domain.models.Sujet
import app.partners.pnsa.features.user.domain.models.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoxDiscussionRow(
    sujet: Sujet,
    isSelect: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val unreadCount = sujet.replyCount.coerceAtLeast(1)
    val lastComment = sujet.commentaires.lastOrNull()
    val previewMessage = lastComment?.commentaire ?: excerpt(sujet.description, 60).ifBlank { sujet.headline }
    val timeLabel = formatShortTime(sujet.publishedAt)
    val sentByMe = lastComment?.user?.id == 1L

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .background(if (isSelect) wsColor else Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.padding(end = 12.dp)) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(60))
                        .background(wsColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = sujet.authorName.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = bagdeColor,
                    )
                }
                if (isSelect) {
                    Badge(
                        containerColor = bagdeColor,
                        modifier = Modifier
                            .size(22.dp)
                            .absoluteOffset(y = 28.dp, x = 28.dp)
                            .border(
                                width = 2.dp,
                                color = Color(0xF788F18A),
                                shape = CircleShape,
                            ),
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = sujet.headline,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PnsaInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PnsaGreenSoft,
                    ) {
                        Text(
                            text = sujet.categoryLabel,
                            color = bagdeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sentByMe) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = bagdeColor,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp),
                        )
                    }
                    Text(
                        text = previewMessage,
                        color = Color.Black.copy(alpha = 0.55f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = timeLabel,
                color = if (unreadCount > 0) bagdeColor else Color.Black.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = if (unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
            )
            Spacer(Modifier.height(6.dp))
            Badge(
                modifier = Modifier.size(22.dp),
                containerColor = bagdeColor,
            ) {
                Text(
                    text = unreadCount.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    senderName: String,
    messageText: String,
    timeText: String,
    isSentByMe: Boolean,
    statusRead: Boolean? = true,
    audioDurationSeconds: Int? = null,
    audioPath: String? = null,
) {
    val voicePayload = remember(messageText, audioDurationSeconds, audioPath) {
        VoiceMessageCodec.decode(messageText, audioDurationSeconds, audioPath)
    }
    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isSentByMe) 18.dp else 4.dp,
        bottomEnd = if (isSentByMe) 4.dp else 18.dp,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = if (isSentByMe) Alignment.End else Alignment.Start,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(min = if (voicePayload != null) 235.dp else 90.dp, max = 300.dp)
                .clip(shape)
                .clipToBounds(),
            shape = shape,
            color = if (isSentByMe) PnsaGreenBubbleSent else Color.White,
            border = BorderStroke(
                width = 1.dp,
                color = if (isSentByMe) bagdeColor.copy(alpha = 0.28f) else Color(0xFFE2ECE3),
            ),
            shadowElevation = 1.dp,
        ) {
            Column(
                Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clipToBounds(),
            ) {
                if (!isSentByMe && senderName.isNotBlank()) {
                    Text(
                        text = senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = bagdeColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                }
                if (voicePayload != null) {
                    VoiceNotePlayerRow(payload = voicePayload)
                } else {
                    Text(
                        text = messageText,
                        color = PnsaInk,
                        fontSize = 14.sp,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = timeText,
                        color = Color.Black.copy(alpha = 0.45f),
                        fontSize = 10.sp,
                    )
                    if (isSentByMe) {
                        Icon(
                            imageVector = if (statusRead == true) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (statusRead == true) bagdeColor else Color.Black.copy(alpha = 0.45f),
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceNotePlayerRow(
    payload: VoicePayload,
) {
    val player = remember { PlatformVoicePlayer() }
    val totalMs = remember(payload.durationSeconds) {
        (payload.durationSeconds.coerceAtLeast(1) * 1000)
    }
    var isPlaying by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableIntStateOf(0) }
    var usingHardwareAudio by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            player.stop()
        }
    }

    LaunchedEffect(isPlaying, totalMs) {
        while (isPlaying) {
            delay(100L)
            if (usingHardwareAudio && player.isPlaying()) {
                val pos = player.currentPositionMs()
                val dur = player.durationMs().takeIf { it > 0 } ?: totalMs
                elapsedMs = pos.coerceAtMost(dur)
            } else {
                val next = elapsedMs + 100
                if (next >= totalMs) {
                    elapsedMs = 0
                    isPlaying = false
                    player.stop()
                } else {
                    elapsedMs = next
                }
            }
        }
    }

    val progress = (elapsedMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
    val displaySeconds = if (isPlaying || elapsedMs > 0) {
        (elapsedMs / 1000).coerceIn(0, payload.durationSeconds)
    } else {
        payload.durationSeconds
    }
    val waveformHeights = remember {
        listOf(7, 13, 18, 10, 21, 15, 9, 23, 17, 11, 20, 14, 22, 12, 16, 19, 8, 15, 21, 13, 10, 16, 11, 8)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier
                .size(38.dp)
                .quietClick {
                    if (isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else {
                        if (elapsedMs >= totalMs) elapsedMs = 0
                        usingHardwareAudio = player.play(payload.audioPath) {
                            isPlaying = false
                            elapsedMs = 0
                        }
                        if (usingHardwareAudio && elapsedMs > 0) {
                            player.seekTo(elapsedMs)
                        }
                        isPlaying = true
                    }
                },
            shape = CircleShape,
            color = bagdeColor,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Lire le vocal",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                val activeBars = (progress * waveformHeights.size).toInt()
                waveformHeights.forEachIndexed { index, barHeight ->
                    val active = if (isPlaying || elapsedMs > 0) index <= activeBars else false
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(barHeight.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (active) bagdeColor else bagdeColor.copy(alpha = 0.28f),
                            )
                            .quietClick {
                                val targetRatio = (index + 1).toFloat() / waveformHeights.size.toFloat()
                                val targetMs = (targetRatio * totalMs).toInt().coerceIn(0, totalMs)
                                elapsedMs = targetMs
                                if (usingHardwareAudio) {
                                    player.seekTo(targetMs)
                                }
                            },
                    )
                }
            }
            Spacer(Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (isPlaying) PnsaPink else bagdeColor,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = if (isPlaying || elapsedMs > 0) {
                            "${formatVoiceTimer(displaySeconds)} / ${formatVoiceTimer(payload.durationSeconds)}"
                        } else {
                            formatVoiceTimer(payload.durationSeconds)
                        },
                        color = PnsaInk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = if (isPlaying) "Lecture…" else "Audio",
                    color = PnsaMuted,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
fun WhatsAppComposerBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Écrire un message…",
    sending: Boolean = false,
    onSend: () -> Unit,
    onSendVoice: ((durationSeconds: Int, audioPath: String?) -> Unit)? = null,
) {
    val recorder = remember { PlatformVoiceRecorder() }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var recordingTicks by remember { mutableIntStateOf(0) }
    var amplitude by remember { mutableIntStateOf(40) }

    DisposableEffect(Unit) {
        onDispose {
            recorder.cancelRecording()
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            recordingTicks = 0
            while (isRecording) {
                delay(200L)
                recordingTicks += 1
                recordingSeconds = recordingTicks / 5
                amplitude = recorder.maxAmplitude().coerceIn(20, 100)
            }
        }
    }

    val startVoiceCapture = rememberEnsureAudioPermission {
        recorder.startRecording()
        recordingSeconds = 0
        recordingTicks = 0
        isRecording = true
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clipToBounds()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        if (isRecording) {
            IconButton(
                onClick = {
                    recorder.cancelRecording()
                    isRecording = false
                    recordingSeconds = 0
                    recordingTicks = 0
                },
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = PnsaPink,
                    containerColor = PnsaPink.copy(alpha = 0.12f),
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Annuler le vocal",
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clipToBounds(),
                shape = RoundedCornerShape(24.dp),
                color = PnsaChatCanvas,
                border = BorderStroke(1.dp, PnsaPink.copy(alpha = 0.45f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (recordingTicks % 4 < 2) Color(0xFFE53935) else Color(0xFFE53935).copy(alpha = 0.3f),
                                ),
                        )
                        Text(
                            text = formatVoiceTimer(recordingSeconds),
                            color = PnsaInk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .height(22.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        val baseWave = listOf(8, 14, 18, 10, 20, 12, 16, 20, 9, 15, 19, 11, 17, 13)
                        baseWave.forEachIndexed { idx, base ->
                            val dynamicFactor = ((recordingTicks + idx * 2) % 5) * (amplitude / 25)
                            val barH = (base + dynamicFactor).coerceIn(6, 22)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(barH.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(PnsaPink),
                            )
                        }
                    }
                    Text(
                        text = "Enregistrement…",
                        color = PnsaMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val savedPath = recorder.stopRecording()
                    val finalDuration = recordingSeconds.coerceAtLeast(1)
                    isRecording = false
                    recordingSeconds = 0
                    recordingTicks = 0
                    onSendVoice?.invoke(finalDuration, savedPath)
                },
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White,
                    containerColor = bagdeColor,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Envoyer le vocal",
                    modifier = Modifier.size(20.dp),
                )
            }
        } else {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp, max = 120.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clipToBounds(),
                shape = RoundedCornerShape(24.dp),
                color = PnsaChatCanvas,
                border = BorderStroke(1.dp, bagdeColor.copy(alpha = 0.30f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clipToBounds()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        maxLines = 4,
                        cursorBrush = SolidColor(bagdeColor),
                        textStyle = TextStyle(color = PnsaInk, fontSize = 15.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clipToBounds(),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clipToBounds(),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (value.isEmpty()) {
                                    Text(
                                        text = placeholder,
                                        color = PnsaMuted,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (value.isNotBlank() && !sending) {
                        onSend()
                    } else if (value.isBlank() && !sending && onSendVoice != null) {
                        startVoiceCapture()
                    }
                },
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White,
                    containerColor = bagdeColor,
                ),
            ) {
                Icon(
                    imageVector = if (value.isBlank()) Icons.Default.Mic else Icons.Default.Send,
                    contentDescription = if (value.isBlank()) "Enregistrer un vocal" else "Envoyer",
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
fun InlineConversationHeader(
    title: String,
    subtitle: String,
    initials: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(36.dp).quietClick(onClick = onBack),
            shape = CircleShape,
            color = wsColor,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = bagdeColor, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(60))
                .background(bagdeColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PnsaInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = bagdeColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AdviceListScreen(navigator: AppNavigator) {
    val graph = LocalAppGraph.current
    var items by remember { mutableStateOf<List<Discussion>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val fallbackDiscussions = remember {
        listOf(
            Discussion(id = 1L, libelle = "Conseiller SSR · Hilaire", updatedAt = "22:10"),
            Discussion(id = 2L, libelle = "Ligne d’orientation · Gedeon", updatedAt = "19:45"),
            Discussion(id = 3L, libelle = "Accompagnement confidentiel", updatedAt = "15:12"),
        )
    }

    LaunchedEffect(Unit) {
        runCatching { graph.community.discussions() }
            .onSuccess { items = it.data.ifEmpty { fallbackDiscussions } }
            .onFailure { items = fallbackDiscussions }
        loading = false
    }

    PnsaScaffold { padding ->
        PageBackdrop {
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White),
            ) {
                InlineConversationHeader(
                    title = "Échanges confidentiels",
                    subtitle = "Avec un conseiller SSR",
                    initials = "EC",
                    onBack = { navigator.pop() },
                )
                HorizontalDivider(color = bagdeColor.copy(alpha = 0.15f))

                when {
                    loading -> LoadingState()
                    items.isEmpty() -> EmptyState(
                        "Aucune discussion pour l’instant",
                        "Un conseiller habilité ouvrira la conversation. Contact PNSA : ${AppConfig.contactPhone}.",
                    )
                    else -> LazyColumn(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clipToBounds(),
                    ) {
                        items(items, key = { it.id ?: it.title.hashCode().toLong() }) { discussion ->
                            val discId = discussion.id ?: 1L
                            BoxDiscussionRow(
                                sujet = Sujet(
                                    id = discId,
                                    title = discussion.title,
                                    description = "Échange confidentiel · Appuie pour ouvrir la conversation",
                                    categorie = Categorie(libelle = "Privé"),
                                    user = User(prenom = discussion.title.take(2)),
                                    commentairesCount = 1,
                                    updatedAt = discussion.updatedAt ?: discussion.createdAt ?: "12:13",
                                ),
                                onClick = { navigator.push(AppDestination.AdviceChat(discId)) },
                            )
                            HorizontalDivider(
                                color = Color(0xFFF0F4F0),
                                modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdviceChatScreen(id: Long, onBack: () -> Unit) {
    val graph = LocalAppGraph.current
    val user by graph.session.user.collectAsState()
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var value by rememberSaveable(id) { mutableStateOf(graph.prefs.getString("advice_draft_$id").orEmpty()) }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val fallbackMessages = remember(id) {
        val savedJson = graph.prefs.getString("advice_msgs_$id")
        val initial = if (!savedJson.isNullOrBlank()) {
            runCatching {
                app.partners.pnsa.core.network.AppJson.decodeFromString(
                    kotlinx.serialization.builtins.ListSerializer(Message.serializer()),
                    savedJson,
                )
            }.getOrNull()
        } else {
            null
        } ?: listOf(
            Message(
                id = 1L,
                msg = "Bonjour ! Bienvenue sur l’espace d’échange confidentiel du PNSA. Comment puis-je t’orienter aujourd’hui ?",
                userId = 99L,
                createdAt = "15:10",
                status = true,
            ),
            Message(
                id = 2L,
                msg = "Tes échanges restent strictement privés.",
                userId = 99L,
                createdAt = "15:12",
                status = true,
            ),
        )
        mutableStateListOf<Message>().apply { addAll(initial) }
    }

    fun persistAdviceMessages(list: List<Message>) {
        runCatching {
            val encoded = app.partners.pnsa.core.network.AppJson.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(Message.serializer()),
                list,
            )
            graph.prefs.putString("advice_msgs_$id", encoded)
        }
    }

    fun load() {
        scope.launch {
            loading = true
            runCatching { graph.community.messages(id) }
                .onSuccess {
                    val data = it.data.ifEmpty { fallbackMessages }
                    messages = data
                    persistAdviceMessages(data)
                }
                .onFailure { messages = fallbackMessages.toList() }
            loading = false
        }
    }

    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0

    LaunchedEffect(id) { load() }

    LaunchedEffect(messages.size, imeVisible) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PnsaChatCanvas)
            .windowInsetsPadding(
                WindowInsets.ime
                    .union(WindowInsets.navigationBars)
                    .only(WindowInsetsSides.Bottom),
            )
            .clipToBounds(),
    ) {
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f),
        ) {
            InlineConversationHeader(
                title = "Conseiller SSR #$id",
                subtitle = "En ligne · Échange confidentiel",
                initials = "CS",
                onBack = onBack,
            )
        }

        if (loading) {
            Box(Modifier.weight(1f).fillMaxWidth()) { LoadingState() }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    item {
                        Box(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                            StatusBanner("Les notifications n’affichent jamais le texte de tes messages.")
                        }
                    }
                    items(messages, key = { it.id ?: it.msg.orEmpty().hashCode().toLong() }) { message ->
                        val mine = (message.userId != null && message.userId == user?.id) || message.userId == 1L
                        ChatMessageBubble(
                            senderName = if (mine) "Toi" else "Conseiller SSR",
                            messageText = message.msg.orEmpty(),
                            timeText = formatShortTime(message.createdAt),
                            isSentByMe = mine,
                            statusRead = message.status,
                        )
                    }
                }
            }
        }

        WhatsAppComposerBar(
            value = value,
            onValueChange = {
                value = it
                graph.prefs.putString("advice_draft_$id", it.ifBlank { null })
            },
            placeholder = "Écrire un message…",
            sending = sending,
            onSend = {
                val trimmed = value.trim()
                if (trimmed.isEmpty()) return@WhatsAppComposerBar
                scope.launch {
                    sending = true
                    runCatching { graph.community.sendMessage(id, trimmed) }
                        .onSuccess {
                            value = ""
                            graph.prefs.putString("advice_draft_$id", null)
                            load()
                        }
                        .onFailure {
                            val newMsg = Message(
                                id = (messages.size + 100).toLong(),
                                msg = trimmed,
                                userId = user?.id ?: 1L,
                                createdAt = "12:13",
                                status = true,
                            )
                            fallbackMessages.add(newMsg)
                            val updated = fallbackMessages.toList()
                            messages = updated
                            persistAdviceMessages(updated)
                            value = ""
                            graph.prefs.putString("advice_draft_$id", null)
                        }
                    sending = false
                }
            },
            onSendVoice = { durationSeconds, audioPath ->
                val encodedVoice = VoiceMessageCodec.encode(durationSeconds, audioPath)
                scope.launch {
                    sending = true
                    runCatching { graph.community.sendMessage(id, encodedVoice) }
                        .onSuccess { load() }
                        .onFailure {
                            val newMsg = Message(
                                id = (messages.size + 100).toLong(),
                                msg = encodedVoice,
                                userId = user?.id ?: 1L,
                                createdAt = "12:13",
                                status = true,
                            )
                            fallbackMessages.add(newMsg)
                            val updated = fallbackMessages.toList()
                            messages = updated
                            persistAdviceMessages(updated)
                        }
                    sending = false
                }
            },
        )
    }
}

private fun formatShortTime(raw: String?): String {
    if (raw.isNullOrBlank()) return "12:13"
    if (raw.length <= 6 && raw.contains(":")) return raw
    return formatIsoDate(raw).ifBlank { "12:13" }
}
