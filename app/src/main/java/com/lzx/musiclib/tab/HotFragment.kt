package com.lzx.musiclib.tab

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bigkoo.convenientbanner.ConvenientBanner
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator
import com.bigkoo.convenientbanner.holder.Holder
import com.gcssloop.widget.RCImageView
import com.lzx.musiclib.R
import com.lzx.musiclib.adapter.addItem
import com.lzx.musiclib.adapter.itemClicked
import com.lzx.musiclib.adapter.setText
import com.lzx.musiclib.adapter.setup
import com.lzx.musiclib.base.BaseFragment
import com.lzx.musiclib.bean.HotSongInfo
import com.lzx.musiclib.bean.MusicBanner
import com.lzx.musiclib.dp
import com.lzx.musiclib.getViewObj
import com.lzx.musiclib.loadImage
import com.lzx.musiclib.setMargins
import com.lzx.musiclib.viewmodel.MusicViewModel
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import kotlinx.android.synthetic.main.fragment_recomment.recycleView

class HotFragment : BaseFragment() {
    companion object {
        fun newInstance(): HotFragment {
            return HotFragment()
        }
    }

    override fun getResourceId(): Int = R.layout.fragment_recomment

    private var viewModel: MusicViewModel? = null

    override fun initView(view: View?) {
        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        viewModel?.getBaiduRankList()
        viewModel?.hotListLiveData?.observe(this, Observer {
            initRecycleView(it)
        })
    }

    private fun initRecycleView(it: Pair<MutableList<MusicBanner>, MutableList<HotSongInfo>>) {
        val list = mutableListOf<Any>()
        list.add(it.first)
        it.second.forEachIndexed { index, hotSongInfo ->
            if (index < 3) {
                list.add(hotSongInfo)
            }
        }
        it.second.getOrNull(3)?.infoList?.forEach {
            list.add(it)
        }
        recycleView?.setup<Any> {
            dataSource(list)
            adapter {
                addItem(R.layout.item_hot_banner) {
                    isForViewType { data, position -> position == 0 }
                    bindViewHolder { data, position, holder ->
                        val bannerList = data as MutableList<MusicBanner>
                        val banner = holder.findViewById<ConvenientBanner<MusicBanner>>(R.id.convenientBanner)
                        banner.setPages(object : CBViewHolderCreator {
                            override fun createHolder(itemView: View?): Holder<MusicBanner> {
                                return object : Holder<MusicBanner>(itemView) {
                                    var imageView: RCImageView? = null
                                    override fun initView(itemView: View?) {
                                        imageView = itemView?.findViewById(R.id.bannerImage)
                                    }

                                    override fun updateUI(data: MusicBanner?) {
                                        imageView?.loadImage(data?.picUrl)
                                    }
                                }
                            }

                            override fun getLayoutId(): Int = R.layout.item_banner
                        }, bannerList)
                        banner.startTurning()
                    }
                }
                addItem(R.layout.item_hot_type_one) {
                    isForViewType { data, position -> position != 0 && data is HotSongInfo && !data.title.isNullOrEmpty() }
                    bindViewHolder { data, position, holder ->
                        val info = data as HotSongInfo
                        setText(R.id.title to info.title)
                        val songLayout = holder.findViewById<LinearLayout>(R.id.songLayout)
                        songLayout.removeAllViews()
                        info.infoList.forEachIndexed { index, songInfo ->
                            if (index < 3) {
                                val view = R.layout.item_hot_song.getViewObj(context)
                                val cover = view.findViewById<RCImageView>(R.id.cover)
                                val songName = view.findViewById<TextView>(R.id.songName)
                                cover.loadImage(songInfo.songCover)
                                songName.text = songInfo.songName
                                songLayout.addView(view)
                                if (index == 0) {
                                    view.setMargins(3.dp.toInt(), 0, 2.dp.toInt(), 0)
                                } else {
                                    view.setMargins(0, 0, 2.dp.toInt(), 0)
                                }
                                view.setOnClickListener {
                                    StarrySky.with().playMusic(info.infoList, index)
                                }
                            }
                        }
                    }
                }
                addItem(R.layout.item_recomment_channel) {
                    isForViewType { data, position -> position != 0 && data is SongInfo }
                    bindViewHolder { data, position, holder ->
                        val info = data as SongInfo
                        val icon = holder.findViewById<RCImageView>(R.id.cover)
                        icon.loadImage(info.songCover)
                        setText(R.id.title to info.songName, R.id.desc to "热门推荐", R.id.username to data.artist)
                        itemClicked(View.OnClickListener {
                            StarrySky.with().playMusicByInfo(info)
                        })
                    }
                }
            }
        }
    }

    override fun unInitView() {
    }
}