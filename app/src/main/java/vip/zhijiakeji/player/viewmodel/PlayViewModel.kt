package vip.zhijiakeji.player.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import vip.zhijiakeji.player.entiey.NovelInfo

class PlayViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences =
        application.getSharedPreferences("playInfo", Context.MODE_PRIVATE)

    private val pathKey = "playPath"
    private val timeKey = "playTime"

    var novelInfoList = ArrayList<NovelInfo>()
        set(value) {
            field = value

            val previousPath = getPreviousPath()

            previousPath?.let { voiceName.value = getVoiceName(it) }

            previousPath.let {
                for (index in 0..value[0].voiceList.lastIndex) {
                    if (it == value[0].voiceList[index]) {
                        playIndex.value = index
                    }
                }
            }
        }

    val playIndex: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val choiceVoice: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val voiceName: MutableLiveData<String> = MutableLiveData<String>()


    fun getPreviousPath(): String? {
        return sharedPreferences.getString(pathKey, null)
    }

    fun getVoiceName(index: Int): String {
        val srt = novelInfoList[0].voiceList[index]

        val srts = srt.split("/")

        return srts[srts.size - 1].replace(".mp3", "")
    }

    fun getVoiceName(path: String?): String {
        return path?.let {
            val srts = path.split("/")
            srts[srts.size - 1].replace(".mp3", "")
        } ?: "未获取到文件"
    }

    fun getPreviousTime(): Int {
        return sharedPreferences.getInt(timeKey, 0)
    }

    fun setNowPlayInfo(path: String, time: Int) {
        val edit = sharedPreferences.edit()
        edit.putString(pathKey, path)
        edit.putInt(timeKey, time)
        edit.apply()
    }
}