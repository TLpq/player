package vip.zhijiakeji.player.recycadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import vip.zhijiakeji.player.R
import vip.zhijiakeji.player.dialog.VoiceListDialog
import vip.zhijiakeji.player.entiey.NovelInfo
import java.io.File

abstract class NovelMenuRecyclerAdapter(var novelInfoList: ArrayList<NovelInfo>) :
    RecyclerView.Adapter<NovelMenuRecyclerAdapter.NovelMenuViewHolder>() {
    abstract fun onChoiceListener(novelName: String)

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
                .load("${novelInfo.novelPath}${File.separator}icon.png")
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView)

            novelName.text = novelInfo.novelName

            itemView.setOnClickListener {
                val dialog = object : VoiceListDialog(it.context, novelInfo.voiceList) {
                    override fun onChoiceVoiceListener(novelName: String) {
                        onChoiceListener(novelName)
                    }
                }
                dialog.show()
            }
        }
    }

    override fun getItemCount() = novelInfoList.size
}