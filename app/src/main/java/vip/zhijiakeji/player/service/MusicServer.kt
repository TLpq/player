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
import vip.zhijiakeji.player.MainActivity
import vip.zhijiakeji.player.R
import java.util.*

class MusicServer : Service() {

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
     * ???????????????
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
     * ?????? or ????????????
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
     * ??????????????????
     */
    fun seekTo(msec: Int) {
        mediaPlayer.seekTo(msec)
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