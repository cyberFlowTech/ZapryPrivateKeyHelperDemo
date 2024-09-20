package com.cyberflow.mimolite.lib_private_key.event

import android.content.Context

/**
 * @title
 * @author Darren.eth
 * @Date
 */
sealed class PrivateKeyFlowEvent {
    class ShowAuthSettingDialogEvent(val context: Context) : PrivateKeyFlowEvent()
}