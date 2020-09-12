package com.lzx.musiclib

import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gcssloop.widget.RCImageView
import com.lzx.musiclib.adapter.addItem
import com.lzx.musiclib.adapter.setText
import com.lzx.musiclib.adapter.setup
import com.lzx.musiclib.base.BaseFragment
import com.lzx.musiclib.bean.MusicChannel
import kotlinx.android.synthetic.main.fragment_recomment.recycleView

class RecommendFragment : BaseFragment() {

    companion object {
        fun newInstance(): RecommendFragment {
            return RecommendFragment()
        }
    }

    override fun getResourceId(): Int = R.layout.fragment_recomment

    private var viewModel: MusicViewModel? = null

    override fun initView(view: View?) {
        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        viewModel?.getQQMusicRecommend()
        viewModel?.musicChannelLiveData?.observe(this, Observer {
            initRecycleView(it)
        })
    }

    private fun initRecycleView(list: MutableList<MusicChannel>) {
        activity?.showToast("list = " + list.size)
        recycleView.setup<MusicChannel> {
            dataSource(list)
            adapter {
                addItem(R.layout.item_recomment_channel) {
                    bindViewHolder { data, position, holder ->
                        val icon = holder.findViewById<RCImageView>(R.id.cover)
                        icon.loadImage(data?.cover)
                        setText(R.id.title to data?.title, R.id.desc to data?.rcmdtemplate, R.id.username to data?.username)
                    }
                }
            }
        }
    }

    override fun unInitView() {
    }

}