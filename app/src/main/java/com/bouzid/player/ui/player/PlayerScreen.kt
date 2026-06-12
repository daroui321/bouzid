package com.bouzid.player.ui.player

import androidx.compose.animation.*
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
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.bouzid.player.data.Channel
import com.bouzid.player.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(viewModel: PlayerViewModel, onChangeUrl: (() -> Unit)? = null) {
    val state by viewModel.state.collectAsState()
    var showChannels by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        when (val s = state) {
            is PlayerUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GoldPrimary)
                        Spacer(Modifier.height(12.dp))
                        Text("Loading channels…", color = TextSecondary)
                    }
                }
            }
            is PlayerUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            (state as PlayerUiState.Error).message,
                            color = TextSecondary,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            }
            is PlayerUiState.ChannelsLoaded -> {
                val selected = s.selectedChannel ?: return

                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        VideoPlayer(channel = selected, isPlaying = s.isPlaying)

                        ChannelInfoOverlay(
                            channel = selected,
                            channelCount = s.channels.size,
                            showChannels = showChannels,
                            onToggleChannels = { showChannels = !showChannels },
                            onChangeUrl = onChangeUrl
                        )
                    }
                }

                if (showChannels) {
                    ChannelsDrawer(
                        viewModel = viewModel,
                        state = s,
                        onDismiss = { showChannels = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoPlayer(channel: Channel, isPlaying: Boolean) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = isPlaying
            setMediaItem(MediaItem.fromUri(channel.url))
            prepare()
        }
    }

    DisposableEffect(channel) {
        exoPlayer.stop()
        exoPlayer.setMediaItem(MediaItem.fromUri(channel.url))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        onDispose { }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

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

@Composable
private fun ChannelInfoOverlay(
    channel: Channel,
    channelCount: Int,
    showChannels: Boolean,
    onToggleChannels: () -> Unit,
    onChangeUrl: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.6f),
                        Color.Transparent,
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.4f)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bouzid",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = channel.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 280.dp)
                    )
                }
                Row {
                    if (onChangeUrl != null) {
                        IconButton(
                            onClick = onChangeUrl,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SurfaceDark)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Change URL",
                                tint = TextSecondary
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    IconButton(
                        onClick = onToggleChannels,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark)
                    ) {
                        Icon(
                            if (showChannels) Icons.Default.Close else Icons.Default.List,
                            contentDescription = "Channels",
                            tint = GoldPrimary
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = channel.group.ifBlank { "General" },
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "$channelCount channels",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChannelsDrawer(
    viewModel: PlayerViewModel,
    state: PlayerUiState.ChannelsLoaded,
    onDismiss: () -> Unit
) {
    val channels = viewModel.getFilteredChannels()
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = SurfaceDark,
            shadowElevation = 16.dp
        ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Channels",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onDismiss) {
                    Text("Close", color = GoldPrimary)
                }
            }

            if (state.groups.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 48.dp)
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedGroup == null,
                            onClick = { viewModel.selectGroup(null) },
                            label = { Text("All", fontSize = 12.sp) },
                            modifier = Modifier.padding(end = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = BackgroundDark
                            )
                        )
                    }
                    items(state.groups) { group ->
                        FilterChip(
                            selected = state.selectedGroup == group,
                            onClick = { viewModel.selectGroup(group) },
                            label = {
                                Text(
                                    group.ifBlank { "General" },
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.padding(end = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = BackgroundDark
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
                        isSelected = channel.url == state.selectedChannel?.url,
                        onClick = {
                            viewModel.selectChannel(channel)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun ChannelItem(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Tv,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    color = if (isSelected) GoldPrimary else TextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (channel.group.isNotBlank()) {
                    Text(
                        text = channel.group,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
            if (isSelected) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
