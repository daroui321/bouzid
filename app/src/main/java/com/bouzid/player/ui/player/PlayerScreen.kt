package com.bouzid.player.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.bouzid.player.data.Channel
import com.bouzid.player.ui.theme.*

@Composable
fun PlayerScreen(viewModel: PlayerViewModel, onChangeUrl: (() -> Unit)? = null) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        when (val s = state) {
            is PlayerUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GoldPrimary)
                        Spacer(Modifier.height(12.dp))
                        Text("Loading channels\u2026", color = TextSecondary)
                    }
                }
            }
            is PlayerUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Warning, contentDescription = null,
                            tint = ErrorRed, modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            s.message, color = TextSecondary,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            }
            is PlayerUiState.ChannelsLoaded -> {
                PlayerContent(
                    state = s,
                    viewModel = viewModel,
                    onChangeUrl = onChangeUrl
                )
            }
        }
    }
}

@Composable
private fun PlayerContent(
    state: PlayerUiState.ChannelsLoaded,
    viewModel: PlayerViewModel,
    onChangeUrl: (() -> Unit)?
) {
    var selected by remember { mutableStateOf(state.selectedChannel) }
    var playing by remember { mutableStateOf(false) }
    var showChannels by remember { mutableStateOf(true) }
    var playerError by remember { mutableStateOf<String?>(null) }

    val channelToPlay = if (playing) selected else null

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (channelToPlay != null) {
                VideoPlayer(
                    channel = channelToPlay,
                    onError = { playerError = it }
                )
            }

            PlayerOverlay(
                channel = selected,
                channelCount = state.channels.size,
                isPlaying = playing,
                playerError = playerError,
                showChannels = showChannels,
                onChangeUrl = onChangeUrl,
                onToggleChannels = { showChannels = !showChannels },
                onPlay = {
                    selected = state.selectedChannel
                    playing = true
                    playerError = null
                }
            )
        }
    }

    if (showChannels) {
        ChannelsDrawer(
            channels = viewModel.getFilteredChannels(),
            state = state,
            selectedChannel = selected,
            onSelect = { ch ->
                selected = ch
                playing = true
                playerError = null
                showChannels = false
            },
            onSelectGroup = { viewModel.selectGroup(it) },
            onDismiss = { showChannels = false }
        )
    }
}

@Composable
private fun VideoPlayer(channel: Channel, onError: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val exoPlayer = remember {
        try {
            ExoPlayer.Builder(context).build().apply {
                playWhenReady = true
                setMediaItem(MediaItem.fromUri(channel.url))
                prepare()
            }
        } catch (e: Exception) {
            onError("Player error: ${e.localizedMessage ?: "Unknown"}")
            null
        }
    }

    DisposableEffect(channel) {
        if (exoPlayer != null) {
            exoPlayer.stop()
            exoPlayer.setMediaItem(MediaItem.fromUri(channel.url))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
        onDispose { }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer?.release() }
    }

    if (exoPlayer != null) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = true
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    setShowFastForwardButton(false)
                    setShowRewindButton(false)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PlayerOverlay(
    channel: Channel?,
    channelCount: Int,
    isPlaying: Boolean,
    playerError: String?,
    showChannels: Boolean,
    onChangeUrl: (() -> Unit)?,
    onToggleChannels: () -> Unit,
    onPlay: () -> Unit
) {
    val bgMod = if (isPlaying && playerError == null)
        Modifier.background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.4f))))
    else
        Modifier.background(Color.Black.copy(alpha = 0.85f))

    Box(modifier = Modifier.fillMaxSize().then(bgMod)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Bouzid", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (channel != null) {
                        Text(
                            channel.name, color = TextPrimary,
                            fontWeight = FontWeight.SemiBold, fontSize = 18.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 280.dp)
                        )
                    }
                }
                Row {
                    if (onChangeUrl != null) {
                        IconButton(
                            onClick = onChangeUrl,
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(SurfaceDark)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Change URL", tint = TextSecondary)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    IconButton(
                        onClick = onToggleChannels,
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(SurfaceDark)
                    ) {
                        Icon(
                            if (showChannels) Icons.Default.Close else Icons.Default.List,
                            contentDescription = "Channels", tint = GoldPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            if (!isPlaying || playerError != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val err = playerError
                    if (err != null) {
                        Text(err, color = ErrorRed, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                    }
                    if (channel != null && !isPlaying) {
                        Text(
                            "Select a channel to start watching",
                            color = TextSecondary, fontSize = 16.sp
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onPlay,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BackgroundDark),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Play", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    channel?.group?.ifBlank { "General" } ?: "",
                    color = TextSecondary, fontSize = 12.sp
                )
                Text("$channelCount channels", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChannelsDrawer(
    channels: List<Channel>,
    state: PlayerUiState.ChannelsLoaded,
    selectedChannel: Channel?,
    onSelect: (Channel) -> Unit,
    onSelectGroup: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.45f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = SurfaceDark,
        shadowElevation = 16.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Channels", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                TextButton(onClick = onDismiss) { Text("Close", color = GoldPrimary) }
            }

            if (state.groups.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 48.dp).padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedGroup == null,
                            onClick = { onSelectGroup(null) },
                            label = { Text("All", fontSize = 12.sp) },
                            modifier = Modifier.padding(end = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary, selectedLabelColor = BackgroundDark
                            )
                        )
                    }
                    items(state.groups) { group ->
                        FilterChip(
                            selected = state.selectedGroup == group,
                            onClick = { onSelectGroup(group) },
                            label = { Text(group.ifBlank { "General" }, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.padding(end = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary, selectedLabelColor = BackgroundDark
                            )
                        )
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                items(channels) { channel ->
                    ChannelItem(
                        channel = channel,
                        isSelected = channel.url == selectedChannel?.url,
                        onClick = { onSelect(channel) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelItem(channel: Channel, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Tv, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    channel.name,
                    color = if (isSelected) GoldPrimary else TextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                if (channel.group.isNotBlank()) {
                    Text(channel.group, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
                }
            }
            if (isSelected) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}
