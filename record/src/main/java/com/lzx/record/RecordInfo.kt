package com.lzx.record

import android.os.Parcel
import android.os.Parcelable

class RecordInfo(val name: String,
                 val format: String,
                 var duration: Long,
                 val size: Long,
                 val location: String,
                 val created: Long,
                 val sampleRate: Int,
                 val channelCount: Int,
                 val bitrate: Int,
                 val isInTrash: Boolean) : Parcelable {

    var waveForm: IntArray? = null
    var data: ByteArray? = null


    val nameWithExtension: String
        get() = "$name.$format"

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(format)
        parcel.writeLong(duration)
        parcel.writeLong(size)
        parcel.writeString(location)
        parcel.writeLong(created)
        parcel.writeInt(sampleRate)
        parcel.writeInt(channelCount)
        parcel.writeInt(bitrate)
        parcel.writeByte(if (isInTrash) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RecordInfo> {
        override fun createFromParcel(parcel: Parcel): RecordInfo {
            return RecordInfo(parcel)
        }

        override fun newArray(size: Int): Array<RecordInfo?> {
            return arrayOfNulls(size)
        }
    }
}