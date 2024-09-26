package com.zapry.pkdemo.rn

import com.facebook.react.bridge.Promise
import com.zapry.pkdemo.model.MultiWalletInfo
import com.zapry.pkdemo.model.RNWalletInfo

interface RNActionInterface {
    fun removePrivateKey(promise: Promise) {}

    @Deprecated("set getWalletByChainCode", ReplaceWith("getWalletByChainCode"))
    fun getWalletInfo(promise: Promise) {
    }
//
    @Deprecated("see setMultiWalletInfo", ReplaceWith("setMultiWalletInfo"))
    fun setWalletInfo(walletInfo: RNWalletInfo) {
    }

    fun scan(promise: Promise) {}
    fun dismissLoading() {}
    fun getWalletByChainCode(chainCode: String): Map<String, String>
    fun setMultiWalletInfo(walletInfo: MultiWalletInfo) {}
//    fun putLogs(log: String?) {
//        log?.apply {
//            ReportLogHelper.getInstance().reportLog(this)
//        }
//    }

    fun otherAction(action: String, params: String?, promise: Promise) {
        promise.reject(Exception("Invalid action: $action"))
    }
}