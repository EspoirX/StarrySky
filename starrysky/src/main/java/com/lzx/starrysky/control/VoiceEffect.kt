package com.lzx.starrysky.control

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.utils.StarrySkyConstant

/**
 * 音效
 */
class VoiceEffect {

    private var isInit = false
    private var equalizer: Equalizer? = null      //均衡器
    private var bassBoost: BassBoost? = null      //低音增强
    private var virtualizer: Virtualizer? = null  //环绕声
    private var currSessionId: Int = -1

    private fun effectSwitchIsOpen(): Boolean {
        return StarrySkyConstant.keyEffectSwitch
    }

    /**
     * 根据 audioSessionId 应用音效(注意这个方法没有加开关判断)
     */
    fun attachAudioEffect(audioSessionId: Int) {
        if (audioSessionId == 0) {
            currSessionId = -1
        }
        if (currSessionId == audioSessionId) return

        releaseAudioEffect()

        StarrySky.log("audioSessionId = $audioSessionId")
        if (audioSessionId == 0) return

        currSessionId = audioSessionId

        equalizer = Equalizer(1, audioSessionId)
        bassBoost = BassBoost(1, audioSessionId)
        virtualizer = Virtualizer(1, audioSessionId)

        //是否读取本地配置
        val attachLocalConfig = StarrySkyConstant.keySaveEffectConfig
        if (attachLocalConfig) {
            equalizer?.applySettings(StarrySkyConstant.keyEqualizerSetting)
            bassBoost?.applySettings(StarrySkyConstant.keyBassBoostSetting)
            virtualizer?.applySettings(StarrySkyConstant.keyVirtualizerSetting)
        }

        equalizer?.setControlStatusListener { _, _ ->
            applyChanges()
        }

        bassBoost?.setControlStatusListener { _, _ ->
            applyChanges()
        }

        virtualizer?.setControlStatusListener { _, _ ->
            applyChanges()
        }

        equalizer?.enabled = true
        bassBoost?.enabled = true
        virtualizer?.enabled = true

        isInit = true
    }

    /**
     * 应用音效参数改变
     */
    fun applyChanges() {
        updateConfig(equalizer?.properties.toString(), bassBoost?.properties.toString(), virtualizer?.properties.toString())
    }

    /**
     * 应用音效参数改变，参数自定义
     */
    fun updateConfig(effectConfig: String?, bassBoostConfig: String?, virtualizerConfig: String?) {
        if (!effectSwitchIsOpen()) return
        if (!isInit) return
        val attachLocalConfig = StarrySkyConstant.keySaveEffectConfig
        if (equalizer?.hasControl() == true && !effectConfig.isNullOrEmpty()) {
            equalizer?.applySettings(effectConfig)
            if (attachLocalConfig) {
                StarrySkyConstant.keyEqualizerSetting = effectConfig
            }
        }
        if (bassBoost?.hasControl() == true && !bassBoostConfig.isNullOrEmpty()) {
            bassBoost?.applySettings(bassBoostConfig)
            if (attachLocalConfig) {
                StarrySkyConstant.keyBassBoostSetting = bassBoostConfig
            }
        }
        if (virtualizer?.hasControl() == true && !virtualizerConfig.isNullOrEmpty()) {
            virtualizer?.applySettings(virtualizerConfig)
            if (attachLocalConfig) {
                StarrySkyConstant.keyVirtualizerSetting = virtualizerConfig
            }
        }
    }

    fun equalizer() = equalizer

    fun bassBoost() = bassBoost

    fun virtualizer() = virtualizer

    /**
     * 根据给定的预设设置均衡器。
     */
    fun equalizerUsePreset(preset: Short) {
        if (!effectSwitchIsOpen()) return
        if (!isInit) return
        equalizer?.usePreset(preset)
    }

    /**
     * 将给定的均衡器频段设置为给定的增益值。
     */
    fun equalizerBandLevel(band: Short, level: Short) {
        if (!effectSwitchIsOpen()) return
        if (!isInit) return
        equalizer?.setBandLevel(band, level)
    }

    /**
     * 获取均衡器支持的预设总数。 预设将具有索引[0，预设数量-1]。
     */
    fun equalizerNumberOfPresets(): Short {
        return equalizer?.numberOfPresets ?: 0
    }

    /**
     * 根据索引获取预设名称。
     */
    fun equalizerPresetName(preset: Short): String {
        return equalizer?.getPresetName(preset).orEmpty()
    }

    /**
     * 获取供{@link #setBandLevel（short，short）}使用的级别范围。 级别以毫贝表示。
     */
    fun equalizerBandLevelRange(): ShortArray {
        return equalizer?.bandLevelRange ?: ShortArray(0)
    }

    /**
     * 获取均衡器引擎支持的频带数。
     */
    fun equalizerNumberOfBands(): Short {
        return equalizer?.numberOfBands ?: 0
    }

    /**
     * 获取给定均衡器频段的增益设置。
     */
    fun equalizerBandLevel(band: Short): Short {
        return equalizer?.getBandLevel(band) ?: 0
    }

    /**
     * 获取给定频段的中心频率。
     */
    fun equalizerCenterFreq(band: Short): Int {
        return equalizer?.getCenterFreq(band) ?: 0
    }

    /**
     * 获取当前预设。
     */
    fun equalizerCurrentPreset(): Short {
        return equalizer?.currentPreset ?: 0
    }

    /**
     * 获取效果的当前强度。
     */
    fun bassBoostRoundedStrength(): Short {
        return bassBoost?.roundedStrength ?: 0
    }

    /**
     * 获取效果的当前强度。
     */
    fun virtualizerStrength(): Short {
        return virtualizer?.roundedStrength ?: 0
    }

    /**
     * 设置低音增强效果的强度。 如果实现不支持设置强度的精确度，则可以将给定的强度四舍五入到最接近的支持值。
     * 您可以使用{@link #getRoundedStrength（）}方法来查询实际设置的（可能是四舍五入的）值。
     */
    fun bassBoostStrength(strength: Short) {
        if (!effectSwitchIsOpen()) return
        if (!isInit) return
        bassBoost?.setStrength(strength)
    }

    /**
     * 设置虚拟器效果的强度。 如果实现不支持设置强度的精确度，则可以将给定的强度四舍五入到最接近的支持值。
     * 您可以使用{@link #getRoundedStrength（）}方法来查询实际设置的（可能是四舍五入的）值。
     */
    fun virtualizerStrength(strength: Short) {
        if (!effectSwitchIsOpen()) return
        if (!isInit) return
        virtualizer?.setStrength(strength)
    }

    /**
     * 释放资源
     */
    fun releaseAudioEffect() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        equalizer = null
        bassBoost = null
        virtualizer = null
        isInit = false
        currSessionId = -1
    }
}

fun Equalizer.applySettings(config: String?) {
    if (config.isNullOrEmpty()) return
    try {
        this.properties = Equalizer.Settings(config)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun BassBoost.applySettings(config: String?) {
    if (config.isNullOrEmpty()) return
    try {
        this.properties = BassBoost.Settings(config)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun Virtualizer.applySettings(config: String?) {
    if (config.isNullOrEmpty()) return
    try {
        this.properties = Virtualizer.Settings(config)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun String.equalizerPresetName(): String {
    when (this) {
        "Normal" -> return "正常"
        "Classical" -> return "古典"
        "Dance" -> return "舞蹈"
        "Flat" -> return "平坦"
        "Folk" -> return "民谣"
        "Heavy Metal" -> return "重金属"
        "Hip Hop" -> return "嘻哈"
        "Jazz" -> return "爵士"
        "Pop" -> return "流行"
        "Rock" -> return "摇滚"
    }
    return this
}