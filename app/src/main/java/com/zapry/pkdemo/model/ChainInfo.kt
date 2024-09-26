package com.zapry.pkdemo.model


import com.google.gson.annotations.SerializedName

data class ChainInfo(
    @SerializedName("address")
    val address: String? = null, // 0xAf540a7058cF02b1CEC416d47F195A9255219592
    @SerializedName("privateKey")
    val privateKey: String? = null // 0x9d205f774323be4cb440b724ab4f4c4d141e91ca50994e97f95d480c3eeb75e7
)