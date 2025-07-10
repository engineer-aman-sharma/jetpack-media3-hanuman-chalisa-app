package com.spiritual.hub.ui

import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.Player.PositionInfo
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.spiritual.hub.R
import com.spiritual.hub.viewmodel.ChalisaViewModel
import kotlinx.coroutines.delay

// MainScreen by @engineer-aman-sharma

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var controller by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var totalDuration by remember { mutableStateOf(1L) }
    val vm: ChalisaViewModel = hiltViewModel()
    var isUserSeeking by remember { mutableStateOf(false) }

    // Establish connection with Media3 AudioService
    LaunchedEffect(Unit) {
        val token = SessionToken(context, ComponentName(context, AudioService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            controller = future.get()
            controller?.let { ctrl ->
                totalDuration = ctrl.duration.coerceAtLeast(1L)
                ctrl.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingState: Boolean) {
                        isPlaying = isPlayingState
                    }

                    override fun onPositionDiscontinuity(
                        oldPosition: PositionInfo, newPosition: PositionInfo, reason: Int
                    ) {
                        if (!isUserSeeking) {
                            currentPosition = newPosition.positionMs
                        }
                    }

                    override fun onEvents(player: Player, events: Player.Events) {
                        if (!isUserSeeking) {
                            currentPosition = player.currentPosition
                        }
                    }
                })
            }
        }, MoreExecutors.directExecutor())
    }

    // Keep updating UI with current playback position
    LaunchedEffect(controller) {
        while (true) {
            controller?.let {
                if (!isUserSeeking) {
                    currentPosition = it.currentPosition
                    totalDuration = it.duration.coerceAtLeast(1L)
                }
            }
            delay(100)
        }
    }

    // Toggle between play and pause, and start foreground service if needed
    fun togglePlayback() {
        controller?.let {
            if (isPlaying) {
                it.pause()
            } else {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, AudioService::class.java)
                )
                it.prepare()
                it.play()
            }
        }
    }

    // Release MediaController when Composable is removed
    DisposableEffect(Unit) {
        onDispose {
            controller?.pause()
            controller?.release()
            controller = null
        }
    }

    Scaffold(bottomBar = {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatMillis(currentPosition), fontSize = 12.sp)
                    Text(formatMillis(totalDuration), fontSize = 12.sp)
                }

                // Seek slider: allow user to change position manually
                Slider(
                    value = currentPosition.toFloat().coerceIn(0f, totalDuration.toFloat()),
                    onValueChange = {
                        currentPosition = it.toLong()
                        isUserSeeking = true
                    },
                    onValueChangeFinished = {
                        controller?.seekTo(currentPosition)
                        isUserSeeking = false
                    },
                    valueRange = 0f..totalDuration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFF5722),
                        activeTrackColor = Color(0xFFFFCCBC),
                        inactiveTrackColor = Color(0xFFBDBDBD)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { togglePlayback() }) {
                        Icon(
                            painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                            contentDescription = null,
                            modifier = Modifier.size(55.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(25.dp))

                    IconButton(onClick = {
                        controller?.let {
                            it.seekTo(0L)
                            it.pause()
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_stop),
                            contentDescription = null,
                            modifier = Modifier.size(55.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Faded background image
            Image(
                painter = painterResource(R.drawable.background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.2f
            )

            // Hanuman Chalisa UI
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "हनुमान चालीसा",
                    fontSize = 32.sp,
                    color = Color(0xFFB71C1C),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = vm.hanumanText,
                    fontSize = 25.sp,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 100.dp)
                )
            }
        }
    }
}

// Helper to format milliseconds to mm:ss
fun formatMillis(ms: Long): String {
    val s = ms / 1000
    return "%02d:%02d".format(s / 60, s % 60)
}

@Preview(showBackground = true)
@Composable
fun PreviewHome() = HomeScreen()