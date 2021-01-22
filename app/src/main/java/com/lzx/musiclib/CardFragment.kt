package com.lzx.musiclib

import android.os.Bundle
import android.view.View
import com.lzx.musiclib.adapter.CardAdapter
import com.lzx.musiclib.base.BaseFragment
import com.lzx.musiclib.viewmodel.MusicViewModel
import com.lzx.musiclib.weight.GalleryItemDecoration
import com.lzx.musiclib.weight.OnViewPagerListener
import com.lzx.musiclib.weight.ViewPagerLayoutManager
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.utils.orDef
import kotlinx.android.synthetic.main.fragment_card.recycleView

class CardFragment : BaseFragment() {
    override fun getResourceId(): Int = R.layout.fragment_card
    private var viewModel: MusicViewModel? = null

    companion object {
        fun newInstance(cardType: String, cardName: String) = CardFragment().apply {
            arguments = Bundle().apply {
                putString("cardType", cardType)
                putString("cardName", cardName)
            }
        }
    }

    private var cardType: String? = null
    private var cardName: String? = null
    private var linearLayoutManager: ViewPagerLayoutManager? = null
    private var cardAdapter: CardAdapter? = null
    var curPlayPos = 0
    private var isVisibleToUser: Boolean = false

    override fun initView(view: View?) {
        cardType = arguments?.getString("cardType")
        cardName = arguments?.getString("cardName")
        viewModel = getSelfViewModel {
            cardLiveData.observe(this@CardFragment, {
                cardAdapter?.submitList(it, true)
            })
        }
        initRecycleView()
        viewModel?.getCardMusicList(cardType)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
        if (isVisibleToUser) {
            val position = linearLayoutManager?.getCurrPosition().orDef()
            if (position >= 0 && position <= cardAdapter?.getList()?.lastIndex ?: 0) {
                curPlayPos = position
            }
            playCurVoice(curPlayPos)
        }
    }

    private fun initRecycleView() {
        cardAdapter = CardAdapter(activity)
        linearLayoutManager = ViewPagerLayoutManager(activity)
        linearLayoutManager?.recycleChildrenOnDetach = true
        recycleView.layoutManager = linearLayoutManager
        recycleView.addItemDecoration(GalleryItemDecoration())
        recycleView.adapter = cardAdapter
        linearLayoutManager?.setOnViewPagerListener(object : OnViewPagerListener {
            override fun onInitComplete() {
                val position = linearLayoutManager?.getCurrPosition().orDef()
                if (position >= 0 && position <= cardAdapter?.getList()?.lastIndex ?: 0) {
                    curPlayPos = position
                }
                playCurVoice(curPlayPos)
            }

            override fun onPageRelease(isNext: Boolean, position: Int) {

            }

            override fun onPageSelected(position: Int, isBottom: Boolean) {
                if (curPlayPos == position) return
                playCurVoice(position)
            }
        })
    }

    fun playCurVoice(position: Int) {
        if (!isVisibleToUser) return
        curPlayPos = position
        val songInfo = cardAdapter?.getItem(position) ?: return
        activity?.showToast("当前播放：" + songInfo.songName)
        StarrySky.with()
            .withOutCallback()
            .skipMediaQueue(true)
            .playMusicByInfo(songInfo)
    }

    override fun unInitView() {
    }
}