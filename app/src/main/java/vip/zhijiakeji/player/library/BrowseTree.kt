package vip.zhijiakeji.player.library

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageVolume
import android.support.v4.media.MediaMetadataCompat
import androidx.annotation.RequiresApi
import java.io.File

@RequiresApi(Build.VERSION_CODES.R)
class BrowseTree(context: Context, storageVolumeList: MutableList<StorageVolume>) {
    private val mediaIdToChildren = mutableMapOf<String, MutableList<MediaMetadataCompat>>()

    init {
        val rootList = mediaIdToChildren[PLAY_BROWSABLE_ROOT] ?: mutableListOf()

        storageVolumeList.forEach {
            if (it.state == Environment.MEDIA_MOUNTED) {
                it.directory?.let { path ->
                    val file = File(path, PLAY_BROWSABLE_ROOT)
                    if (file.exists()) {
                        val albumsNovel = file.listFiles { p0 -> p0?.isDirectory ?: false }

                        albumsNovel?.forEach { albums ->
                            val albumsMetadata = MediaMetadataCompat.Builder().apply {
                                putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, albums.path)
                                putString(MediaMetadataCompat.METADATA_KEY_TITLE, albums.name)
                                putString(
                                    MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                                    Uri.fromFile(File(albums, "icon.png")).toString()
                                )
                            }.build()

                            rootList += albumsMetadata
                        }

                    }
                }
            }

        }

        mediaIdToChildren[PLAY_BROWSABLE_ROOT] = rootList


    }
}

const val PLAY_BROWSABLE_ROOT = "Audio novel"