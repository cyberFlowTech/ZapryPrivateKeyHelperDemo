package com.zapry.pkdemo.event


sealed class FlowEvent {
    object ShowLoadingEvent : FlowEvent()
}