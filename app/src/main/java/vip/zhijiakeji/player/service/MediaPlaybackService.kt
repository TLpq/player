package vip.zhijiakeji.player.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.PowerManager
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import vip.zhijiakeji.player.MediaPlayerActivity
import vip.zhijiakeji.player.R
import vip.zhijiakeji.player.util.PersistentStorage

private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

class MediaPlaybackService : MediaBrowserServiceCompat(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener {

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private lateinit var storage: PersistentStorage

    private val mediaPlayer: MediaPlayer by lazy {
        MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        }
    }

    override fun onCreate() {
        super.onCreate()

        // 构建可用于启动 UI 的 PendingIntent。
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        // 创建 MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "MediaPlaybackService").apply {
            setSessionActivity(sessionActivityPendingIntent)

            // 使用 ACTION_PLAY 设置初始 PlaybackState，以便媒体按钮可以启动播放器
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
            setPlaybackState(stateBuilder.build())

            // MySessionCallback() 具有处理来自媒体控制器的回调的方法
            // setCallback(MySessionCallback())

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }

        initNotification()
    }

    override fun onDestroy() {
        super.onDestroy()


        //mediaPlayer?.release()
        //mediaPlayer = null
    }

    /*override fun onBind(intent: Intent): IBinder? {
        return MediaPlaybackServiceBind()
    }

    inner class MediaPlaybackServiceBind : Binder() {
        val service: MediaPlaybackService
            get() = this@MediaPlaybackService
    }*/

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
            // 为顶层构建 MediaItem 对象，并将它们放入 mediaItems 列表中...
        }else{
            // 检查传递的 parentMediaId 以查看我们所在的子菜单，并将该菜单的子项放入 mediaItems 列表中...
        }*/

        result.sendResult(mediaItems[0])
    }

    private fun initNotification() {
        // 给定一个媒体会话及其上下文（通常是包含会话的组件） 创建一个 NotificationCompat.Builder 获取会话的元数据
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
            // 添加当前播放曲目的元数据
            setContentTitle("Wonderful music")
            setContentText("My Awesome Band")
            //setSubText("My Awesome setSubText")
            setShowWhen(false)
            //setLargeIcon(description.iconBitmap)

            // 通过单击通知启用播放器
            //setContentIntent(controller.sessionActivity)

            // 当通知被刷掉时停止服务
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this@MediaPlaybackService,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            // 使传输控制在锁定屏幕上可见
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // 添加应用图标并设置其强调色 注意颜色
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

            // 利用 MediaStyle 功能
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0)

                    // 添加取消按钮
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this@MediaPlaybackService,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
        }

        // 显示通知并将服务置于前台
        startForeground(1001, builder.build())
    }

    // 当 MediaPlayer 准备好时调用
    override fun onPrepared(mediaPlayer: MediaPlayer?) {
        TODO("Not yet implemented")
    }

    // 当 MediaPlayer 发生错误时调用
    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        TODO("Not yet implemented")
    }
}