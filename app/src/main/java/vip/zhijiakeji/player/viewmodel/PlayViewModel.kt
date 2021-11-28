package vip.zhijiakeji.player.viewmodel

import android.app.Application
import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import vip.zhijiakeji.player.entiey.NovelInfo

class PlayViewModel(application: Application) : AndroidViewModel(application) {

    val albumListLiveData: MutableLiveData<MutableMap<String, MutableList<MediaMetadataCompat>>> by lazy {
        MutableLiveData<MutableMap<String, MutableList<MediaMetadataCompat>>>()
    }

    val playIndex: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val choiceVoice: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val voiceName: MutableLiveData<String> = MutableLiveData<String>()

    /*fun getVoiceName(index: Int): String {
        val srt = novelInfoList[0].voiceList[index]

        val srts = srt.split("/")

        return srts[srts.size - 1].replace(".mp3", "")
    }*/

    fun getVoiceName(path: String?): String {
        return path?.let {
            val srts = path.split("/")
            srts[srts.size - 1].replace(".mp3", "")
        } ?: "未获取到文件"
    }
}