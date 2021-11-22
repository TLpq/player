package vip.zhijiakeji.player

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.storage.StorageManager
import android.util.Log
import android.util.Log.WARN
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import android.view.Menu
import android.view.MenuItem
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import vip.zhijiakeji.player.databinding.ActivityMainBinding
import vip.zhijiakeji.player.entiey.NovelInfo
import vip.zhijiakeji.player.fragment.MenuFragment
import vip.zhijiakeji.player.fragment.PlayFragment
import vip.zhijiakeji.player.fragment.adapter.PlayFragmentAdapter
import vip.zhijiakeji.player.service.MusicServer
import vip.zhijiakeji.player.viewmodel.PlayViewModel
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), MediaPlayer.OnCompletionListener {
    private lateinit var viewModel: PlayViewModel


    private val resPath = "Audio novel"

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var playFragmentAdapter: PlayFragmentAdapter

    private lateinit var viewPager: ViewPager2

    private lateinit var playServer: MusicServer

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder = p1 as MusicServer.MusicServerBind

            playServer = binder.server.apply {
                setPlayCompleteListener(this@MainActivity)
                viewModel.choiceVoice.observe(this@MainActivity, {
                    val uri = Uri.parse(it)
                    viewModel.voiceName.value = viewModel.getVoiceName(it)
                    play(uri)
                })
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {

        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(PlayViewModel::class.java)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)


//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)


        /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()*/

        bindService(
            Intent(applicationContext, MusicServer::class.java),
            mConnection,
            Context.BIND_AUTO_CREATE
        )



        dff()
        //getFile()
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

    @RequiresApi(Build.VERSION_CODES.R)
    fun getFile() {
        val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val novelInfoList = ArrayList<NovelInfo>()
        for (sv in storageManager.storageVolumes) {
            try {
                sv.directory?.let {
                    val files = File(it.path).listFiles() { _, p1 -> p1 == resPath }
                    for (file in files!!) {
                        val novelFiles = file.listFiles() { p0 -> p0.isDirectory }

                        for (novelName in novelFiles!!) {
                            val voices = novelName.listFiles() { _, name -> name.endsWith(".mp3") }
                            val list = ArrayList<String>()
                            for (info in voices) {
                                list.add(info.path)
                            }
                            list.sortWith { p0, p1 ->
                                p0.compareTo(p1)
                            }

                            val novelInfo = NovelInfo(novelName.path, novelName.name, "", list)
                            novelInfoList.add(novelInfo)
                        }
                    }
                }
            } catch (e: Exception) {
                //e.printStackTrace()
            }
        }


        viewModel.novelInfoList = novelInfoList
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    /**
     * 播放完成回调
     */
    override fun onCompletion(p0: MediaPlayer?) {
        val index = viewModel.playIndex.value!! + 1
        val uri = Uri.parse(viewModel.novelInfoList[0].voiceList[index])
        viewModel.playIndex.value = index
        viewModel.voiceName.value = viewModel.getVoiceName(uri.path)
        playServer.play(uri)
    }

    /**
     * 申请权限
     */
    private fun dff() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        when {
            checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                requestPermissions(permissions, 107)
            }

            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                //val ss = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, 107)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            107 -> {

            }
        }
    }
}