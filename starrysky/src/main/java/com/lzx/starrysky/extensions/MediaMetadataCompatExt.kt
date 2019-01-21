package com.lzx.starrysky.extensions

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DataSource

/**
 * MediaMetadataCompat 扩展 （获取相关字段）
 */
//id
inline val MediaMetadataCompat.id get() = getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
//音乐标题
inline val MediaMetadataCompat.title get() = getString(MediaMetadataCompat.METADATA_KEY_TITLE)
//音乐艺术家
inline val MediaMetadataCompat.artist get() = getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
//音乐长度
inline val MediaMetadataCompat.duration get() = getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
//专辑名称
inline val MediaMetadataCompat.album get() = getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
//媒体作者
inline val MediaMetadataCompat.author get() = getString(MediaMetadataCompat.METADATA_KEY_AUTHOR)
////媒体作家
inline val MediaMetadataCompat.writer get() = getString(MediaMetadataCompat.METADATA_KEY_WRITER)
//媒体的作曲家。
inline val MediaMetadataCompat.composer get() = getString(MediaMetadataCompat.METADATA_KEY_COMPOSER)
//媒体的编译状态。
inline val MediaMetadataCompat.compilation get() = getString(MediaMetadataCompat.METADATA_KEY_COMPILATION)
//媒体创建或发布的日期
inline val MediaMetadataCompat.date get() = getString(MediaMetadataCompat.METADATA_KEY_DATE)
//媒体创建或发布的那一年。
inline val MediaMetadataCompat.year
    @SuppressLint("WrongConstant")
    get() = getString(MediaMetadataCompat.METADATA_KEY_YEAR)
//媒体的流派。
inline val MediaMetadataCompat.genre get() = getString(MediaMetadataCompat.METADATA_KEY_GENRE)
//媒体的曲目编号。
inline val MediaMetadataCompat.trackNumber get() = getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER)
//媒体原始来源中的曲目数量。
inline val MediaMetadataCompat.trackCount get() = getLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS)
//媒体原始来源的光盘编号。
inline val MediaMetadataCompat.discNumber get() = getLong(MediaMetadataCompat.METADATA_KEY_DISC_NUMBER)
//这位媒体原创专辑的艺术家。
inline val MediaMetadataCompat.albumArtist get() = getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST)
//媒体的封面（Bitmap），封面应该相对较小，可以按比例缩小，如果太大，应该使用{@link #METADATA_KEY_ART_URI}。
inline val MediaMetadataCompat.art get() = getBitmap(MediaMetadataCompat.METADATA_KEY_ART)
//媒体的封面（Uri）
inline val MediaMetadataCompat.artUri get() = Uri.parse(this.getString(MediaMetadataCompat.METADATA_KEY_ART_URI))
//专辑的封面（Bitmap），封面应该相对较小，可以按比例缩小，如果太大，应该使用{@link #METADATA_KEY_ALBUM_ART_URI}。
inline val MediaMetadataCompat.albumArt
    get() = getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
//专辑的封面（Uri）
inline val MediaMetadataCompat.albumArtUri get() = Uri.parse(this.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))
//用户对媒体的评分。
inline val MediaMetadataCompat.userRating
    @SuppressLint("WrongConstant")
    get() = getLong(MediaMetadataCompat.METADATA_KEY_USER_RATING)
//媒体的整体评级。
inline val MediaMetadataCompat.rating
    @SuppressLint("WrongConstant")
    get() = getLong(MediaMetadataCompat.METADATA_KEY_RATING)
//适合向用户显示的标题。 这通常与{@link #METADATA_KEY_TITLE}相同，但某些格式可能会有所不同。 如果存在，当显示由此元数据描述的媒体时，则应该首选。
inline val MediaMetadataCompat.displayTitle get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)
//适合向用户显示的字幕。 当显示此元数据描述的媒体的第二行时，如果存在，则应优先于其他字段。
inline val MediaMetadataCompat.displaySubtitle get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)
//适合于向用户显示的描述。 当显示此元数据描述的媒体的更多信息时，如果存在，则应优先于其他字段。
inline val MediaMetadataCompat.displayDescription get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION)
//适合向用户显示的图标或缩略图。 显示此元数据描述的媒体图标时，如果存在，则应优先于其他字段。 这必须是{@link Bitmap}。
//图标应该相对较小，如果太大则可以按比例缩小。 对于更高分辨率的图稿，应使用{@link #METADATA_KEY_DISPLAY_ICON_URI}。
inline val MediaMetadataCompat.displayIcon
    get() = getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON)
//同上
inline val MediaMetadataCompat.displayIconUri get() = Uri.parse(this.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI))
//表示内容的Uri格式化字符串。 此值特定于提供内容的服务。 它可以与{@link TransportControls #playFromUri（Uri，Bundle）}一起使用，
// 以便在连接到同一应用程序的{@link MediaBrowserCompat}提供时启动播放。
inline val MediaMetadataCompat.mediaUri get() = Uri.parse(this.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))
//将用于以后离线播放的媒体的下载状态
inline val MediaMetadataCompat.downloadStatus get() = getLong(MediaMetadataCompat.METADATA_KEY_DOWNLOAD_STATUS)

/**
 * 用于存储[MediaMetadataCompat]项是否表示[MediaItem.FLAG_BROWSABLE]或[MediaItem.FLAG_PLAYABLE]项的自定义属性。
 */
@MediaItem.Flags
inline val MediaMetadataCompat.flag
    @SuppressLint("WrongConstant")
    get() = this.getLong(METADATA_KEY_UAMP_FLAGS).toInt()


/**
 * MediaMetadataCompat.Builder 扩展（设置相关字段,对应上面的get）
 */
// These do not have getters, so create a message for the error.
const val NO_GET = "Property does not have a 'get'"

inline var MediaMetadataCompat.Builder.id: String
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, value)
    }

inline var MediaMetadataCompat.Builder.title: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_TITLE, value)
    }

inline var MediaMetadataCompat.Builder.artist: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_ARTIST, value)
    }

inline var MediaMetadataCompat.Builder.album: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_ALBUM, value)
    }

inline var MediaMetadataCompat.Builder.duration: Long
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putLong(MediaMetadataCompat.METADATA_KEY_DURATION, value)
    }

inline var MediaMetadataCompat.Builder.genre: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_GENRE, value)
    }

inline var MediaMetadataCompat.Builder.mediaUri: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, value)
    }

inline var MediaMetadataCompat.Builder.albumArtUri: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, value)
    }

inline var MediaMetadataCompat.Builder.albumArt: Bitmap?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, value)
    }

inline var MediaMetadataCompat.Builder.trackNumber: Long
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, value)
    }

inline var MediaMetadataCompat.Builder.trackCount: Long
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, value)
    }

inline var MediaMetadataCompat.Builder.displayTitle: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, value)
    }

inline var MediaMetadataCompat.Builder.displaySubtitle: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, value)
    }

inline var MediaMetadataCompat.Builder.displayDescription: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, value)
    }

inline var MediaMetadataCompat.Builder.displayIconUri: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, value)
    }

inline var MediaMetadataCompat.Builder.downloadStatus: Long
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putLong(MediaMetadataCompat.METADATA_KEY_DOWNLOAD_STATUS, value)
    }

@MediaItem.Flags
inline var MediaMetadataCompat.Builder.flag: Int
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    @SuppressLint("WrongConstant")
    set(value) {
        putLong(METADATA_KEY_UAMP_FLAGS, value.toLong())
    }

/**
  *用于检索[MediaDescriptionCompat]的自定义属性，其中还包括
  *其附加内容中[MediaMetadataCompat]对象的所有键。
  *宣布元数据更改时，ExoPlayer MediaSession扩展使用这些键。
 */
inline val MediaMetadataCompat.fullDescription: MediaDescriptionCompat
    get() =
        description.also {
            it.extras?.putAll(bundle)
        }

/**
 * MediaMetadataCompat 转 ExtractorMediaSource
 */
fun MediaMetadataCompat.toMediaSource(dataSourceFactory: DataSource.Factory) =
        ExtractorMediaSource.Factory(dataSourceFactory)
                .setTag(fullDescription)
                .createMediaSource(mediaUri)!!

/**
 * 给定[MediaMetadataCompat]对象的[List]构建[ConcatenatingMediaSource]的扩展方法。
 */
fun List<MediaMetadataCompat>.toMediaSource(dataSourceFactory: DataSource.Factory): ConcatenatingMediaSource {
    val concatenatingMediaSource = ConcatenatingMediaSource()
    forEach {
        concatenatingMediaSource.addMediaSource(it.toMediaSource(dataSourceFactory))
    }
    return concatenatingMediaSource
}

//自定义属性，用于保存项目是[MediaItem.FLAG_BROWSABLE]还是[MediaItem.FLAG_PLAYABLE]。
const val METADATA_KEY_UAMP_FLAGS = "com.lzx.starrysky.METADATA_KEY_UAMP_FLAGS"