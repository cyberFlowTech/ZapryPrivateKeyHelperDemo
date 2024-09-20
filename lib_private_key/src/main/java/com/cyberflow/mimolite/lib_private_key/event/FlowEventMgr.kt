package com.cyberflow.mimolite.lib_private_key.event

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter

class FlowEventMgr {
    companion object {
        private const val TAG = "FlowEventMgr"

        val instance by lazy {
            FlowEventMgr()
        }
    }

    private val _eventFlow = MutableSharedFlow<PrivateKeyFlowEvent>()
    private val eventFlow: Flow<PrivateKeyFlowEvent> = _eventFlow.asSharedFlow()

    init {
        Log.d(TAG, "init instance")
    }

    fun observeEvent(event: Class<out PrivateKeyFlowEvent>): Flow<PrivateKeyFlowEvent> {
        return observeEvents(arrayOf(event))
    }

    fun observeEvents(events: Array<Class<out PrivateKeyFlowEvent>>): Flow<PrivateKeyFlowEvent> {
        val names = events.map { it.name }
        return eventFlow.filter {
            names.contains(it.javaClass.name)
        }
    }

    fun observeEvents(events: List<Class<out PrivateKeyFlowEvent>>): Flow<PrivateKeyFlowEvent> {
        val names = events.map { it.name }
        return eventFlow.filter {
            names.contains(it.javaClass.name)
        }
    }

    suspend fun fireEvent(event: PrivateKeyFlowEvent) {
        _eventFlow.emit(event)
    }

}