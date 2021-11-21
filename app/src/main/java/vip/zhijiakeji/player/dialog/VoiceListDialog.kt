package vip.zhijiakeji.player.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import vip.zhijiakeji.player.R
import vip.zhijiakeji.player.recycadapter.VoiceMenuRecyclerAdapter

abstract class VoiceListDialog(context: Context, var novelList: ArrayList<String>) :
    Dialog(context, R.style.style_dialog) {

    abstract fun onChoiceVoiceListener(novelName: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_voice_list, null).apply {
            val recyclerView = findViewById<RecyclerView>(R.id.rv_voice_list)
            recyclerView.layoutManager =
                StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

            recyclerView.adapter = object : VoiceMenuRecyclerAdapter(novelList) {
                override fun onChoiceListener(novelName: String) {
                    onChoiceVoiceListener(novelName)
                }

            }
        }

        setContentView(view)

        window?.let {
            it.setGravity(Gravity.BOTTOM)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setWindowAnimations(R.style.dialog_animation)
        }
    }
}