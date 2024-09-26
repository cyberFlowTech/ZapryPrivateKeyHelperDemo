package com.zapry.pkdemo.model


import com.google.gson.annotations.SerializedName

data class MultiWalletInfo(
    @SerializedName("mnemonic")
    val mnemonic: String, // sword bike ozone dinosaur bright hover then ginger cannon toy oppose differ
    @SerializedName("wallet")
    val wallet: Map<String, ChainInfo>,
    @SerializedName("extData")
    val extData: ExtData?,
) {
    data class ExtData(
        @SerializedName("backupId")
        val backupId: String?,
    )
}

