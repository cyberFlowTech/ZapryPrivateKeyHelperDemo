package com.zapry.pkdemo.rn

import com.facebook.react.bridge.Promise
import com.zapry.pkdemo.model.MultiWalletInfo
import com.zapry.pkdemo.model.RNWalletInfo

interface RNActionInterface {

    fun dismissLoading() {}

    fun setMultiWalletInfo(walletInfo: MultiWalletInfo) {}

    fun otherAction(action: String, params: String?, promise: Promise) {
        promise.reject(Exception("Invalid action: $action"))
    }

}