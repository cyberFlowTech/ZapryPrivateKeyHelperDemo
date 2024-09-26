package com.zapry.pkdemo.event

/**
 * JS事件名
 *
 * 方向：
 * 类型1: 原生 --> RN前端
 * 类型2: 原生 --> RN前端 --> 原生
 */

sealed class JSEventName {
    abstract val name: String

    /**
     * RN钱包入账事件
     */
    object WalletReceivedMoney : JSEventName() {
        override val name: String
            get() = "Notify_PAY_Notification"
    }

    /**
     * 钱包解绑？
     */
    object WalletJostle : JSEventName() {
        override val name: String
            get() = "Notify_Wallet_Jostle"
    }

    /**
     * 退出钱包页面
     */
    object WalletExit : JSEventName() {
        override val name: String
            get() = "Notify_Wallet_Exit"
    }

    /**
     * 发送交易
     */
    object SendTransaction : JSEventName() {
        override val name: String
            get() = "Notify_Send_Transaction"
    }

    /**
     * 获取余额
     */
    object GetBalance : JSEventName() {
        override val name: String
            get() = "Notify_Get_Balance"
    }

    /**
     * 签名。（比如tron这种，原生没有签名能力）
     */
    object SignNotEth : JSEventName() {
        override val name: String
            get() = "Notify_Sign"
    }

    object GetBalanceV2 : JSEventName() {
        override val name: String
            get() = "walletFetchBalance"
    }

    object GetSuggestGas : JSEventName() {
        override val name: String
            get() = "walletFetchSuggestGas"
    }

    object SendTx : JSEventName() {
        override val name: String
            get() = "walletTransaction"
    }

    object SignEip712 : JSEventName() {
        override val name: String
            get() = "walletEIP712Sign"
    }

    object GetTxInfo : JSEventName() {
        override val name: String
            get() = "walletFetchTransactionInfo"
    }
}