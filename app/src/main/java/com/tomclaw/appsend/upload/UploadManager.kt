package com.tomclaw.appsend.upload

import com.jakewharton.rxrelay3.BehaviorRelay
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.Executors
import java.util.concurrent.Future

interface UploadManager {

    fun status(id: String): Observable<Int>

    fun upload(id: String, file: String)

}

class UploadManagerImpl : UploadManager {

    private val executor = Executors.newSingleThreadExecutor()

    private val relays = HashMap<String, BehaviorRelay<Int>>()
    private val uploads = HashMap<String, Future<*>>()

    override fun status(id: String): Observable<Int> {
        val relay = relays[id] ?: let {
            val relay = BehaviorRelay.createDefault(IDLE)
            relay.accept(IDLE)
            relays[id] = relay
            relay
        }
        return relay.doFinally {
            println("[upload] Finally status relay")
            if (relay.hasObservers()) {
                println("[upload] Relay $id has observers")
                return@doFinally
            }
            val inactiveState = relay.hasValue() &&
                    (relay.value == IDLE || relay.value == COMPLETED || relay.value == ERROR)
            println("[upload] Relay $id is inactive: $inactiveState")
            if (!relay.hasValue() || inactiveState) {
                relays.remove(id)
                println("[upload] Relay $id removed")
            }
        }
    }

    override fun upload(id: String, file: String) {
        TODO("Not yet implemented")
    }

}

const val IDLE: Int = -30
const val AWAIT: Int = -10
const val STARTED: Int = -20
const val COMPLETED: Int = 101
const val ERROR: Int = -40
