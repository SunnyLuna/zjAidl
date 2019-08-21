package com.decard.myaidl.data

import android.os.Parcel
import android.os.Parcelable

data class MessageModel(val from: String, val to: String, val content: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(from)
        parcel.writeString(to)
        parcel.writeString(content)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "MessageModel(from='$from', to='$to', content='$content')"
    }

    companion object CREATOR : Parcelable.Creator<MessageModel> {
        override fun createFromParcel(parcel: Parcel): MessageModel {
            return MessageModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageModel?> {
            return arrayOfNulls(size)
        }
    }

}