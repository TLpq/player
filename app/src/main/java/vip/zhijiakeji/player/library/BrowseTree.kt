package vip.zhijiakeji.player.library

import android.content.Context
import android.support.v4.media.MediaMetadataCompat

class BrowseTree(context: Context) {
    private val mediaIdToChildren = mutableMapOf<String, MutableList<MediaMetadataCompat>>()

    init {
        val rootList = mediaIdToChildren[UAMP_BROWSABLE_ROOT] ?: mutableListOf()

        val recommendedMetadata = MediaMetadataCompat.Builder().apply {

        }.build()
    }
}

const val UAMP_BROWSABLE_ROOT = "/Audio novel"