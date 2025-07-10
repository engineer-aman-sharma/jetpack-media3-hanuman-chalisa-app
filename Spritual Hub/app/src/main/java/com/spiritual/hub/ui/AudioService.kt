package com.spiritual.hub.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.spiritual.hub.MainActivity
import com.spiritual.hub.R

// AudioService handles Media3 ExoPlayer playback and shows a persistent media notification.
// Built using Jetpack Media3 by @engineer-aman-sharma for Hanuman Chalisa playback.

class AudioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "audio_service_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Audio Service Channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        player = ExoPlayer.Builder(this).build()

        // Listen to playback state changes to manage notification visibility
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        if (player.playWhenReady) {
                            startForeground(NOTIFICATION_ID, createNotification())
                        } else {
                            stopForeground(true)
                        }
                    }

                    Player.STATE_ENDED, Player.STATE_IDLE -> {
                        stopForeground(true)
                        stopSelf()
                    }
                }
            }
        })

        // Set up media metadata and media item
        val metadata = MediaMetadata.Builder()
            .setTitle("हनुमान चालीसा")
            .setArtist("Spiritual Hub")
            .setArtworkUri(Uri.parse("android.resource://$packageName/${R.drawable.s}"))
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse("android.resource://$packageName/${R.raw.hanuman_chalisa}"))
            .setMediaMetadata(metadata)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = false
        player.repeatMode = Player.REPEAT_MODE_ONE

        // Intent to open the app from the notification
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Initialize MediaSession for media controls
        mediaSession = MediaSession.Builder(this, player)
            .setId("SpiritualHubAudioSession")
            .setSessionActivity(pendingIntent)
            .build()
    }

    private fun createNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("हनुमान चालीसा")
            .setContentText("Playing Hanuman Chalisa")
            .setSmallIcon(R.drawable.s)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        stopForeground(true)
        player.stop()
        player.release()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        player.stop()
        stopForeground(true)
        stopSelf()
    }
}