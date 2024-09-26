package com.zapry.pkdemo.model


import com.google.gson.annotations.SerializedName

data class ChainInfo(
    @SerializedName("address")
    val address: String? = null,
    @SerializedName("privateKey")
    val privateKey: String? = null
)