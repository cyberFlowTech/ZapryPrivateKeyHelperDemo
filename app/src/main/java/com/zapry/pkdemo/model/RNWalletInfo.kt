package com.zapry.pkdemo.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RNWalletInfo(
    @SerializedName("address")
    val address: String? = "",
    @SerializedName("mnemonic")
    val mnemonic: String? = "",
    @SerializedName("privateKey")
    val privateKey: String? = ""
) : Parcelable


@Parcelize
data class RNRemovePrivateKeyParam(
    @SerializedName("remove")
    val remove: Int = -1, //1=增加，0=移除
) : Parcelable