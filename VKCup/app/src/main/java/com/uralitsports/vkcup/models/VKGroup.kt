package com.uralitsports.vkcup.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class VKGroup(
        val id: Long = 0,
        val name: String = "",
        val photo: String = "",
        val deactivated: Boolean = false) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeString(photo)
        parcel.writeByte(if (deactivated) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKGroup> {
        override fun createFromParcel(parcel: Parcel): VKGroup {
            return VKGroup(parcel)
        }

        override fun newArray(size: Int): Array<VKGroup?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject)
                = VKGroup(id = json.optLong("id", 0),
                name = json.optString("name", ""),
                photo = json.optString("photo_200", ""),
                deactivated = json.optBoolean("deactivated", false))
    }
}