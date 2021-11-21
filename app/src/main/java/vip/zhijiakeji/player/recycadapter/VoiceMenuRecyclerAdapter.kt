package vip.zhijiakeji.player.recycadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vip.zhijiakeji.player.R

abstract class VoiceMenuRecyclerAdapter(var voiceList: ArrayList<String>) :
    RecyclerView.Adapter<VoiceMenuRecyclerAdapter.VoiceMenuViewHolder>() {

    abstract fun onChoiceListener(novelName: String)

    inner class VoiceMenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var voiceName: TextView = itemView.findViewById(R.id.tv_voice_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoiceMenuViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_voice_menu, parent, false)

        return VoiceMenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoiceMenuViewHolder, position: Int) {
        val voiceInfo = voiceList[position]
        val paths = voiceInfo.split("/")
        val data = paths[paths.size - 1].split(".")
        holder.voiceName.text = data[0]

        holder.itemView.setOnClickListener {
            onChoiceListener(voiceInfo)
        }
    }

    override fun getItemCount() = voiceList.size
}