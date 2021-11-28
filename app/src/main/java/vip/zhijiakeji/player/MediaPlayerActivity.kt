package vip.zhijiakeji.player

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.storage.StorageManager
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import vip.zhijiakeji.player.databinding.ActivityMainBinding
import vip.zhijiakeji.player.fragment.MenuFragment
import vip.zhijiakeji.player.fragment.PlayFragment
import vip.zhijiakeji.player.fragment.adapter.PlayFragmentAdapter
import vip.zhijiakeji.player.library.BrowseTree
import vip.zhijiakeji.player.service.MediaPlaybackService
import vip.zhijiakeji.player.viewmodel.PlayViewModel

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: PlayViewModel

    private lateinit var mediaBrowser: MediaBrowserCompat

    private lateinit var viewPager: ViewPager2
    private lateinit var playFragmentAdapter: PlayFragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.fragment_player)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this).get(PlayViewModel::class.java)

        // ...
        // Create MediaBrowserServiceCompat
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlaybackService::class.java),
            connectionCallbacks,
            null // optional Bundle
        )

        appPermission()
    }

    private fun appPermission() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        var code = 0
        for (index in permissions.indices) {
            if (checkSelfPermission(permissions[index]) == PackageManager.PERMISSION_GRANTED) {
                code++
                if (code == permissions.size) {
                    initRes()
                }
            } else {
                requestPermissions(permissions, 107)
                break
            }
        }

    }

    /**
     * 初始化本地资源
     */
    private fun initRes() {
        val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val browseTree = BrowseTree(storageManager.storageVolumes)
        viewModel.albumListLiveData.postValue(browseTree.mediaIdToChildren)
        initFragmentView()
    }

    private fun initFragmentView() {
        val playFragment = PlayFragment()
        val menuFragment = MenuFragment()
        playFragmentAdapter = PlayFragmentAdapter(
            supportFragmentManager,
            lifecycle,
            arrayListOf(playFragment, menuFragment)
        )

        viewPager = binding.vp.apply {
            isUserInputEnabled = true
            offscreenPageLimit = 2
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = playFragmentAdapter
        }
    }

    public override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    public override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    public override fun onStop() {
        super.onStop()
        // (see "stay in sync with the MediaSession")
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }


    /**
     * 指向控制器的链接，以便处理媒体按钮
     */
    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {

        // 与媒体浏览器服务连接成功
        override fun onConnected() {
            Log.e("MediaBrowserCompat", "服务连接")
            // Get the token for the MediaSession
            mediaBrowser.sessionToken.also { token ->

                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(
                    this@MediaPlayerActivity, // Context
                    token
                )

                // Save the controller
                MediaControllerCompat.setMediaController(this@MediaPlayerActivity, mediaController)
            }

            mediaBrowser.subscribe("2435", object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    val itemsList = children.map {

                    }
                }
            })
            // Finish building the UI
            buildTransportControls()
        }

        // 与浏览器服务的连接丢失时
        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
            Log.e("MediaBrowserCompat", "服务崩溃了。禁用传输控制，直到它自动重新连接")
        }

        // 与媒体浏览器服务的连接失败
        override fun onConnectionFailed() {
            // The Service has refused our connection
            Log.e("MediaBrowserCompat", "服务拒绝了我们的连接")
        }
    }

    /**
     * 为控制播放器的界面元素设置 onClickListeners
     */
    fun buildTransportControls() {
        // 获取控制器，允许应用与正在进行的媒体会话进行交互。媒体按钮和其他命令可以发送到会话
        val mediaController = MediaControllerCompat.getMediaController(this@MediaPlayerActivity)

        mediaController.transportControls.pause()

        mediaController.transportControls.playFromUri(
            Uri.parse("android.resource://" + packageName + "/" + R.raw.qwe),
            null
        )

        // Grab the view for the play/pause button
        /*val playPause = findViewById<ImageView>(R.id.Pause).apply {
            setOnClickListener {
                // Since this is a play/pause button, you'll need to test the current state
                // and choose the action accordingly

                val pbState = mediaController.playbackState.state
                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                    mediaController.transportControls.pause()
                } else {
                    mediaController.transportControls.play()
                }
            }
        }*/


        val pbState11 = mediaController.playbackState.state
        if (pbState11 == PlaybackStateCompat.STATE_PLAYING) {
            mediaController.transportControls.pause()
        } else {
            mediaController.transportControls.play()
        }

        // Display the initial state
        val metadata = mediaController.metadata
        val pbState = mediaController.playbackState

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback)
    }


    /**
     * 媒体会话的状态或元数据每次发生更改时从媒体会话接收回调
     */
    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {}

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {}
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            107 -> {
                initRes()
            }
        }
    }
}