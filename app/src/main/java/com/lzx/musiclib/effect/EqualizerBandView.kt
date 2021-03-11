package com.lzx.musiclib.effect

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.lzx.musiclib.R
import com.lzx.starrysky.StarrySky
import java.util.Locale

class EqualizerBandView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    private val mAllBand = mutableListOf<Band>()
    private val mLayoutInflater = LayoutInflater.from(context)
    private var mOnBandChangeListener: OnBandChangeListener? = null
    private var mOnEqualizerSettingChangeListener: OnEqualizerSettingChangeListener? = null

    init {
        isBaselineAligned = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            layoutDirection = LAYOUT_DIRECTION_LTR
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            // 因为点画线在低于 API 28 的版本中不支持硬件加速，因此需要关闭硬件加速才能生效
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
    }

    fun initData(){
        removeAllViews()
        mAllBand.clear()
        val numberOfBands = StarrySky.effect().equalizerNumberOfBands().toInt()
        for (band in 0 until numberOfBands) {
            addBand(Band(band.toShort()))
        }
    }

    fun setOnBandChangeListener(onBandChangeListener: OnBandChangeListener?) {
        mOnBandChangeListener = onBandChangeListener
        for (band in mAllBand) {
            band.setOnBandChangeListener(mOnBandChangeListener)
        }
    }

    fun setOnEqualizerSettingChangeListener(listener: OnEqualizerSettingChangeListener?) {
        mOnEqualizerSettingChangeListener = listener
        for (band in mAllBand) {
            band.setOnEqualizerSettingChangeListener(mOnEqualizerSettingChangeListener)
        }
    }

    private fun addBand(band: Band) {
        mAllBand.add(band)
        band.setOnBandChangeListener(mOnBandChangeListener)
        band.setOnEqualizerSettingChangeListener(mOnEqualizerSettingChangeListener)
        val itemView = band.createItemView(mLayoutInflater, this)
        val layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT)
        layoutParams.weight = 1.0f
        addView(itemView, layoutParams)
    }

    fun notifyEqualizerSettingChanged() {
        for (band in mAllBand) {
            band.notifyItemDataChanged()
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        for (band in mAllBand) {
            band.seekBar?.isEnabled = enabled
        }
    }

    class Band(private val mBand: Short) {
        var seekBar: VerticalSeekBar? = null
            private set
        private var tvText: TextView? = null
        private var mOnBandChangeListener: OnBandChangeListener? = null
        private var mOnEqualizerSettingChangeListener: OnEqualizerSettingChangeListener? = null

        fun setOnBandChangeListener(onBandChangeListener: OnBandChangeListener?) {
            mOnBandChangeListener = onBandChangeListener
        }

        fun setOnEqualizerSettingChangeListener(onEqualizerSettingChangeListener: OnEqualizerSettingChangeListener?) {
            mOnEqualizerSettingChangeListener = onEqualizerSettingChangeListener
        }

        fun notifyItemDataChanged() {
            val bandLevelRange = StarrySky.effect().equalizerBandLevelRange()
            val bandLevel = StarrySky.effect().equalizerBandLevel(mBand)
            if (bandLevelRange.isEmpty()) return
            val progress = bandLevel - bandLevelRange[0]
            seekBar?.progress = progress
        }

        fun createItemView(inflater: LayoutInflater, parent: ViewGroup): View {
            val itemView = inflater.inflate(R.layout.item_equalizer_band, parent, false)
            seekBar = itemView.findViewById(R.id.seekBar)
            tvText = itemView.findViewById(R.id.tvText)
            initViews()
            return itemView
        }

        @SuppressLint("SetTextI18n")
        private fun initViews() {
            val bandLevelRange = StarrySky.effect().equalizerBandLevelRange()
            val bandLevel = StarrySky.effect().equalizerBandLevel(mBand)
            val centerFreq = StarrySky.effect().equalizerCenterFreq(mBand)
            if (bandLevelRange.isEmpty()) return

            val minLevel = bandLevelRange[0]
            val maxLevel = bandLevelRange[1]
            val max = maxLevel - minLevel
            val center = max / 2
            seekBar?.max = max
            val progress = bandLevel - minLevel
            seekBar?.progress = progress
            seekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        StarrySky.effect().equalizerBandLevel(mBand, (progress - center).toShort())
                        notifyEqualizerSettingChanged()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    if (mOnBandChangeListener != null) {
                        mOnBandChangeListener!!.onBandChanged()
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    StarrySky.effect().applyChanges()
                }
            })
            if (centerFreq >= 1000000) {
                val freq = centerFreq / 1000.0
                tvText?.text = String.format(Locale.ENGLISH, "%.1fkHz", freq / 1000.0)
                return
            }
            tvText?.text = (centerFreq / 1000).toString() + "Hz"
        }

        private fun notifyEqualizerSettingChanged() {
            mOnEqualizerSettingChangeListener?.onEqualizerSettingChanged()
        }
    }

    interface OnBandChangeListener {
        fun onBandChanged()
    }

    interface OnEqualizerSettingChangeListener {
        fun onEqualizerSettingChanged()
    }
}