package com.cyberflow.mimolite.lib_private_key.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PayAuth(
    @SerializedName("data")
    val data: Data? = null,
    @SerializedName("payType")
    val payType: Int? = null // 1：转账: 2：授权，3: 签名，4: 查看助记词，5：解绑钱包
) : Parcelable {
    fun displayAddress(): String {
        val address = data?.to
        if (address.isNullOrEmpty()) {
            return ""
        }
        // 检查字符串长度是否大于11
        return if (address.length > 11) {
            // 如果大于8，只保留前四位和后四位，中间用"..."替代
            address.take(4) + "..." + address.takeLast(4)
        } else {
            // 如果小于或等于11，直接返回原字符串
            address
        }
    }

    fun displayNick(): String {
        return if (data?.nick.isNullOrEmpty()) {
            ""
        } else {
            "[${data?.nick}]"
        }
    }

    @Parcelize
    data class Data(
        @SerializedName("amount")
        val amount: String? = null, // 1
        @SerializedName("chainCode")
        val chainCode: String? = null, // 2000000
        @SerializedName("nick")
        val nick: String? = null,
        @SerializedName("to")
        val to: String? = null, // 0x7b29e41e8dba4926f015530edd3e98e7d4abf4d3
        @SerializedName("token")
        val token: Token? = null,
        @SerializedName("payPassword")
        val payPassword: String? = null, // 123456
        @SerializedName("nftTokenId")
        val nftTokenId: String? = null, // 650495
        @SerializedName("nftName")
        val nftName: String? = null, // Pulsar in the Distance
    ) : Parcelable

    @Parcelize
    data class Token(
        @SerializedName("balance")
        val balance: String? = null, // 50.171922
        @SerializedName("chainCode")
        val chainCode: String? = null, // 2000000
        @SerializedName("chainName")
        val chainName: String? = null, // ETMP
        @SerializedName("contact")
        val contact: String? = null,
        @SerializedName("contract")
        val contract: String? = null,
        @SerializedName("exApi")
        val exApi: String? = null,
        @SerializedName("gasLimit")
        val gasLimit: String? = null, // 100000
        @SerializedName("gasPrice")
        val gasPrice: String? = null, // 0
        @SerializedName("gasSymbol")
        val gasSymbol: String? = null, // ETMP
        @SerializedName("icon")
        val icon: String? = null, // https://res.mimo.immo/chains_icons/tokens/icon-ETMP.png
        @SerializedName("id")
        val id: Int? = null, // 20000000
        @SerializedName("issuer")
        val issuer: String? = null,
        @SerializedName("localStatus")
        val localStatus: Int? = null, // 1
        @SerializedName("name")
        val name: String? = null, // ETMP
        @SerializedName("official")
        val official: Int? = null, // 1
        @SerializedName("packIcon")
        val packIcon: String? = null, // https://res.mimo.immo/chains_icons/pack/ETMP.png
        @SerializedName("scaleShow")
        val scaleShow: Int? = null, // 6
        @SerializedName("sort")
        val sort: Int? = null, // 0
        @SerializedName("token")
        val token: String? = null, // ETMP
        @SerializedName("totalPrice")
        val totalPrice: String? = null, // 0.50
        @SerializedName("unit")
        val unit: Long? = null, // 1000000000000000000
        @SerializedName("unitWei")
        val unitWei: String? = null // ether
    ) : Parcelable {
    }

    companion object {
        const val TRANSFER = 1
        const val CONTRACT_TRADING = 2
        const val SIGN = 3
        const val MNEMONIC = 4
        const val UNBIND = 5
        const val NEW_WALLET = 9
        const val SWITCH_AUTH_VERIFY = 10
        const val ADD_CHAIN = 11
        const val SIGN_EIP712 = 20
    }
}