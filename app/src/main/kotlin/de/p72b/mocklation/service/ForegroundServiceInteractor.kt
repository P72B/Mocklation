package de.p72b.mocklation.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import de.p72b.mocklation.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ForegroundServiceInteractor(private val applicationContext: Context) {

    private val echoReceiver = Pong()
    private var isServiceRunning = false
    private val _status = MutableStateFlow<StatusEvent>(StatusEvent.Stop)

    val status: StateFlow<StatusEvent> = _status

    init {
        checkIfServiceIsUp()
    }

    fun doPlay() {
        if (isServiceRunning) return

        _status.value = StatusEvent.Play
        launchForegroundService()
        isServiceRunning = true
    }

    fun doStop() {
        if (!isServiceRunning) return

        _status.value = StatusEvent.Stop
        stopForegroundService()
        isServiceRunning = false
    }

    private fun launchForegroundService() {
        val intent = Intent(applicationContext, ForegroundService::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        applicationContext.startForegroundService(intent)
    }

    private fun stopForegroundService() {
        val intent = Intent(applicationContext, ForegroundService::class.java)
        applicationContext.stopService(intent)
    }

    private fun checkIfServiceIsUp() {
        try {
            applicationContext.unregisterReceiver(echoReceiver)
        } catch (e: IllegalArgumentException) {
            // not bad that unregister not happend. Better would be to unregister on lifecycle end.
            Logger.d(msg = "echoReceiver not found")
        }

        ContextCompat.registerReceiver(
            applicationContext,
            echoReceiver,
            IntentFilter("pong"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        applicationContext.sendBroadcast(Intent("ping"))
        if (isServiceRunning) {
            _status.value = StatusEvent.Play
        }
    }

    inner class Pong: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            isServiceRunning = true;
        }
    }
}

sealed interface StatusEvent {
    data object Stop : StatusEvent
    data object Play : StatusEvent
    data object Pause : StatusEvent
}