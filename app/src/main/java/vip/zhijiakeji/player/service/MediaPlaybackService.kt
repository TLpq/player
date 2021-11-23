package vip.zhijiakeji.player.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import vip.zhijiakeji.player.MediaPlayerActivity
import vip.zhijiakeji.player.R
import vip.zhijiakeji.player.util.PersistentStorage

private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

class MediaPlaybackService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat
    protected lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private lateinit var storage: PersistentStorage

    override fun onCreate() {
        super.onCreate()

        // Build a PendingIntent that can be used to launch the UI.
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "MediaPlaybackService").apply {
            setSessionActivity(sessionActivityPendingIntent)
            // Enable callbacks from MediaButtons and TransportControls
            /*setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                    or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )*/

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState(stateBuilder.build())

            // MySessionCallback() has methods that handle callbacks from a media controller
            // setCallback(MySessionCallback())

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }


        initNotification()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * 控制客户端连接
     */
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MY_MEDIA_ROOT_ID, null)
    }

    /**
     * 传达内容
     */
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        // MediaItem 对象不应包含图标位图。当您为每项内容构建 MediaDescription 时，请通过调用 setIconUri() 来使用 Uri。
        val mediaItems = emptyList<MediaBrowserCompat.MediaItem>()

        /*if (){
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        }else{
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }*/

        // result.sendResult(mediaItems)
    }

    fun initNotification() {
        // Given a media session and its context (usually the component containing the session)
        // Create a NotificationCompat.Builder

        // Get the session's metadata
//        val controller = mediaSession.controller
//        val mediaMetadata = controller.metadata
//        val description = mediaMetadata.description

        val channelId = "default"
        val channel =
            NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.let {
            if (it.getNotificationChannel(channelId) == null) {//没有创建
                it.createNotificationChannel(channel)//则先创建
            }
        }

        val builder = NotificationCompat.Builder(this, channelId).apply {
            // Add the metadata for the currently playing track
            setContentTitle("Wonderful music")
            setContentText("My Awesome Band")
            //setSubText("My Awesome setSubText")
            setShowWhen(false)
            //setLargeIcon(description.iconBitmap)

            // Enable launching the player by clicking the notification
            //setContentIntent(controller.sessionActivity)

            // Stop the service when the notification is swiped away
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this@MediaPlaybackService,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            // Make the transport controls visible on the lockscreen
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Add an app icon and set its accent color
            // Be careful about the color
            setSmallIcon(R.mipmap.ic_launcher_round)
            color = ContextCompat.getColor(this@MediaPlaybackService, R.color.teal_200)

            // Add a pause button


            val pendingIntent: PendingIntent =
                Intent(this@MediaPlaybackService, MediaPlayerActivity::class.java).let {
                    PendingIntent.getActivity(this@MediaPlaybackService, 0, it, 0)
                }

            addAction(R.drawable.ic_play_up, "Previous", pendingIntent)
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_play_start,
                    "Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@MediaPlaybackService,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                )
            )
            addAction(R.drawable.ic_play_down, "Next", pendingIntent)

            // Take advantage of MediaStyle features
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0)

                    // Add a cancel button
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this@MediaPlaybackService,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
        }

        // Display the notification and place the service in the foreground
        startForeground(1001, builder.build())
    }
}