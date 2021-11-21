package vip.zhijiakeji.player.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import vip.zhijiakeji.player.R
import vip.zhijiakeji.player.viewmodel.PlayViewModel

class PlayFragment : Fragment() {
    private var fragmentView: View? = null

    private lateinit var viewModel: PlayViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentView ?: synchronized(this) {
            fragmentView ?: inflater.inflate(R.layout.fragment_player, container, false).also {
                fragmentView = it
                viewModel = ViewModelProvider(requireActivity()).get(PlayViewModel::class.java)
                val nameView = it.findViewById<TextView>(R.id.player_title)
                nameView.text = viewModel.voiceName.value
                viewModel.voiceName.observe(requireActivity(), { name ->
                    nameView.text = name
                })
            }
        }

        return fragmentView
    }
}