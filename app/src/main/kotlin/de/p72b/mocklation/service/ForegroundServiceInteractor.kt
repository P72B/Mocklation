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

    private val cmdEchoReceiver = CmdPong()
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

    fun doRepeat() {
        if (!isServiceRunning) return
    }

    fun doStop() {
        if (!isServiceRunning) return

        _status.value = StatusEvent.Stop
        stopForegroundService()
        isServiceRunning = false
    }

    fun doPause() {
        if (!isServiceRunning) return

        _status.value = StatusEvent.Pause

        applicationContext.sendBroadcast(
            Intent("cmd").apply {
                putExtra("simulation", "pause")
            }
        )
    }

    fun doResume() {
        if (!isServiceRunning) return

        _status.value = StatusEvent.Play

        applicationContext.sendBroadcast(
            Intent("cmd").apply {
                putExtra("simulation", "resume")
            }
        )
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
            //applicationContext.unregisterReceiver(cmdEchoReceiver)
        } catch (e: IllegalArgumentException) {
            // not bad that unregister not happend. Better would be to unregister on lifecycle end.
            Logger.d(msg = "Broadcast echoReceiver not found")
        }

        ContextCompat.registerReceiver(
            applicationContext,
            cmdEchoReceiver,
            IntentFilter("cmd_pong"),
            ContextCompat.RECEIVER_EXPORTED
        )
        applicationContext.sendBroadcast(Intent("cmd"))
        if (isServiceRunning) {
            _status.value = StatusEvent.Play
        }
    }

    inner class CmdPong : BroadcastReceiver() {
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