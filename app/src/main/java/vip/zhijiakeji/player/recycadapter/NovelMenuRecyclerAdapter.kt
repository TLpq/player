package vip.zhijiakeji.player.recycadapter

import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import vip.zhijiakeji.player.R
import vip.zhijiakeji.player.dialog.VoiceListDialog
import vip.zhijiakeji.player.entiey.NovelInfo
import java.io.File

class NovelMenuRecyclerAdapter(novelInfoMap: MutableMap<String, MutableList<MediaMetadataCompat>>?) :
    RecyclerView.Adapter<NovelMenuRecyclerAdapter.NovelMenuViewHolder>() {
    //abstract fun onChoiceListener(novelName: String)
    private val novelInfoList = ArrayList<MediaMetadataCompat>()

    init {
        novelInfoMap?.forEach() {
            it.value?.let { mutableList ->
                novelInfoList.addAll(mutableList)
            }
        }
    }

    inner class NovelMenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var novelName: TextView

        init {
            itemView.run {
                imageView = findViewById(R.id.ivNovelIcon)
                novelName = findViewById(R.id.tvNovelName)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NovelMenuViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_novel_menu, parent, false)

        return NovelMenuViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NovelMenuViewHolder, position: Int) {
        val novelInfo = novelInfoList[position]

        holder.run {
            Glide.with(holder.itemView.context)
                .load(novelInfo.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView)

            novelName.text = novelInfo.getText(MediaMetadataCompat.METADATA_KEY_TITLE)

            itemView.setOnClickListener {
                /*val dialog = object : VoiceListDialog(it.context, novelInfo.voiceList) {
                    override fun onChoiceVoiceListener(novelName: String) {
                        //onChoiceListener(novelName)
                    }
                }
                dialog.show()*/
            }
        }
    }

    override fun getItemCount() = novelInfoList.size
}