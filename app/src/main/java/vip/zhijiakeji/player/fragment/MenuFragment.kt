package vip.zhijiakeji.player.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import vip.zhijiakeji.player.R
import vip.zhijiakeji.player.recycadapter.NovelMenuRecyclerAdapter
import vip.zhijiakeji.player.viewmodel.PlayViewModel

class MenuFragment : Fragment() {
    private lateinit var viewModel: PlayViewModel

    private var fragmentView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView ?: synchronized(this) {
            fragmentView ?: inflater.inflate(R.layout.fragment_novel_menu, container, false).also {
                viewModel = ViewModelProvider(requireActivity()).get(PlayViewModel::class.java)
                fragmentView = it.apply {
                    val recyclerView = findViewById<RecyclerView>(R.id.rvNovel)
                    recyclerView.layoutManager =
                        StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL)

                    recyclerView.adapter =
                        object : NovelMenuRecyclerAdapter(viewModel.novelInfoList) {
                            override fun onChoiceListener(novelName: String) {
                                viewModel.choiceVoice.value = novelName
                            }

                        }
                }
            }
        }

        return fragmentView
    }
}