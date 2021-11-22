package vip.zhijiakeji.player.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.PlatformVpnProfile
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import vip.zhijiakeji.player.MainActivity
import vip.zhijiakeji.player.R
import java.util.*

class MusicServer : Service() {
    private lateinit var viewMOdel: PlatformVpnProfile

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var mBind: MusicServerBind

    private val pathKey = "playPath"
    private val timeKey = "playTime"

    private lateinit var timer: Timer

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = application.getSharedPreferences("playInfo", Context.MODE_PRIVATE)

        mBind = MusicServerBind()

        initForeground()

        val path = sharedPreferences.getString(pathKey, null)
        val time = sharedPreferences.getInt(timeKey, 0)

        mediaPlayer = path?.let {
            MediaPlayer.create(applicationContext, Uri.parse(it)).apply {
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                seekTo(time)
                start()
            }
        } ?: synchronized(this) {
            MediaPlayer().apply {
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            }
        }

        mediaPlayer.duration

        timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    val edit = sharedPreferences.edit()
                    edit.putInt(timeKey, mediaPlayer.currentPosition)
                    edit.apply()
                }
            }
        }
        timer.schedule(timerTask, 15000, 15000)
    }

    fun setPlayCompleteListener(listener: MediaPlayer.OnCompletionListener) {
        mediaPlayer.setOnCompletionListener(listener)
    }

    fun setOnTimedTextListener(listener: MediaPlayer.OnTimedTextListener) {
        mediaPlayer.setOnTimedTextListener(listener)
    }

    /**
     * 播放新资源
     */
    fun play(uri: Uri) {
        mediaPlayer.run {
            reset()
            setDataSource(applicationContext, uri)
            prepare()
            start()
        }
        val edit = sharedPreferences.edit()
        edit.putString(pathKey, uri.path)
        edit.putInt(timeKey, 0)
        edit.apply()
    }

    /**
     * 暂停 or 开始播放
     */
    fun pauseOrStart() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.prepareAsync()
            mediaPlayer.start()
        }
        val edit = sharedPreferences.edit()
        edit.putInt(timeKey, mediaPlayer.currentPosition)
        edit.apply()
    }

    /**
     * 调整播放位置
     */
    fun seekTo(msec: Int) {
        mediaPlayer.seekTo(msec)
    }

    /**
     * 创建前台任务
     */
    private fun initForeground() {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let {
                PendingIntent.getActivity(this, 0, it, 0)
            }

        val notificationLayout = RemoteViews(packageName, R.layout.notification_play)

        val channelId = "default"
        val channel = NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.let {
            if (it.getNotificationChannel(channelId) == null) {//没有创建
                it.createNotificationChannel(channel)//则先创建
            }
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(pendingIntent)
            .setCustomBigContentView(notificationLayout)
            .build()

        startForeground(1000, notification)
    }

    override fun onDestroy() {
        super.onDestroy()

        val lls = if (mediaPlayer.currentPosition < 5000) {
            0
        } else {
            mediaPlayer.currentPosition - 5000
        }

        val edit = sharedPreferences.edit()
        edit.putInt(timeKey, lls)
        edit.apply()

        mediaPlayer.release()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return mBind
    }


    inner class MusicServerBind : Binder() {
        val server: MusicServer
            get() = this@MusicServer
    }
}