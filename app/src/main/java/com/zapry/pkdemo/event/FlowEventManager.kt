package com.zapry.pkdemo.event

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter

class FlowEventManager {
    companion object {
        private const val TAG = "FlowEventManager"

        val instance by lazy {
            FlowEventManager()
        }
    }

    private val _eventFlow = MutableSharedFlow<FlowEvent>()
    private val eventFlow: Flow<FlowEvent> = _eventFlow.asSharedFlow()

    init {
        Log.d(TAG, "init instance")
    }

    fun observeEvent(event: Class<out FlowEvent>): Flow<FlowEvent> {
        return observeEvents(arrayOf(event))
    }

    fun observeEvents(events: Array<Class<out FlowEvent>>): Flow<FlowEvent> {
        val names = events.map { it.name }
        return eventFlow.filter {
            names.contains(it.javaClass.name)
        }
    }

    fun observeEvents(events: List<Class<out FlowEvent>>): Flow<FlowEvent> {
        val names = events.map { it.name }
        return eventFlow.filter {
            names.contains(it.javaClass.name)
        }
    }

    suspend fun fireEvent(event: FlowEvent) {
        _eventFlow.emit(event)
    }

}