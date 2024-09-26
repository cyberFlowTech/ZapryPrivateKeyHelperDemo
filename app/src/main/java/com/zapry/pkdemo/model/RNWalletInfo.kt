package com.zapry.pkdemo.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RNWalletInfo(
    @SerializedName("address")
    val address: String? = "", // 0x075d393f84b72c91a36fdb0c51773fce07296bde
    @SerializedName("mnemonic")
    val mnemonic: String? = "", // lumber base dose art hurdle sphere economy bubble burst quit road thunder
    @SerializedName("privateKey")
    val privateKey: String? = "" // 0x6c9c32cba86e37cc3cbedde1dac6bd2fe8f272214aa33b887dc4272236e4b43e
) : Parcelable


@Parcelize
data class RNRemovePrivateKeyParam(
    @SerializedName("remove")
    val remove: Int = -1, //1=增加，0=移除
) : Parcelable