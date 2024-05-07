package de.p72b.mocklation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.content.ContextCompat
import de.p72b.mocklation.MainActivity
import de.p72b.mocklation.R
import de.p72b.mocklation.data.MockFeature
import de.p72b.mocklation.data.PreferencesRepository
import de.p72b.mocklation.data.util.Status
import de.p72b.mocklation.service.location.LocationSimulation
import de.p72b.mocklation.service.location.sampler.Instruction
import de.p72b.mocklation.usecase.GetFeatureUseCase
import de.p72b.mocklation.util.Logger
import de.p72b.mocklation.util.roundTo
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject


class ForegroundService : Service() {

    private lateinit var notificationBuilder: Notification.Builder
    private lateinit var notificationManager: NotificationManager
    private val getFeatureUseCase: GetFeatureUseCase by inject()
    private val preferencesRepository: PreferencesRepository by inject()

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "location simulation"
        private const val SERVICE_ID = 72
    }

    private var startMode: Int = 0
    private lateinit var cmdReceiver: ServiceCmdReceiver
    private lateinit var simulation: LocationSimulation
    private lateinit var feature: MockFeature

    override fun onCreate() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        cmdReceiver = ServiceCmdReceiver {
            when (it) {
                "pause" -> {
                    simulation.pause()
                }

                "resume" -> {
                    simulation.resume()
                }

                else -> {
                    Logger.d(msg = "ForegroundService unknown simulation command")
                }
            }
        }

        ContextCompat.registerReceiver(
            applicationContext,
            cmdReceiver,
            IntentFilter("cmd"),
            ContextCompat.RECEIVER_EXPORTED
        )

        preferencesRepository.getSelectedFeature().let {
            if (it.isNullOrEmpty()) {
                stopSelf()
                return
            }
            runBlocking {
                launch {
                    val result = getFeatureUseCase.invoke(it)
                    when (result.status) {
                        Status.SUCCESS -> {
                            if (result.data == null) {
                                stopSelf()
                                return@launch
                            }
                            feature = result.data

                            createNotificationChannel(getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                            notificationBuilder = getServiceNotification()
                            startForeground(SERVICE_ID, notificationBuilder.build())
                        }

                        Status.ERROR -> stopSelf()
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        simulation =
            LocationSimulation(applicationContext, feature) { state -> onSimulationUpdate(state) }
        simulation.play()
        return startMode
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        simulation.stop()
        applicationContext.unregisterReceiver(cmdReceiver)
        super.onDestroy()
    }

    private fun onSimulationUpdate(state: LocationSimulation.SimulationState) {
        when (state) {
            LocationSimulation.SimulationState.Finished -> {
                updateNotificationFinished()
                // TODO finish here maybe
            }

            is LocationSimulation.SimulationState.Status -> {
                when (state.instruction) {
                    is Instruction.FixedInstruction -> updateNoticationFixed(state.instruction)
                    is Instruction.RouteInstruction -> updateNotificationRoute(state.instruction)
                }
            }
        }
    }

    private fun updateNoticationFixed(instruction: Instruction.FixedInstruction) {
        notificationBuilder.setContentText(
            "lat(y): ${instruction.location!!.latitude.roundTo(6)}\n" +
                    "lon(x): ${instruction.location.longitude.roundTo(6)}"
        )
        notificationManager.notify(SERVICE_ID, notificationBuilder.build())
    }

    private fun updateNotificationRoute(instruction: Instruction.RouteInstruction) {
        notificationBuilder.setContentText(
            "Section ${instruction.activeSectionIndex + 1} from ${instruction.totalSectionsIndex + 1}\n" +
                    "lat(y): ${instruction.location!!.latitude.roundTo(5)} / " +
                    "lon(x): ${instruction.location.longitude.roundTo(5)}"
        )
        notificationBuilder.setProgress(100, instruction.progressInPercent.toInt(), false)
        notificationManager.notify(SERVICE_ID, notificationBuilder.build())
    }

    private fun updateNotificationFinished() {
        notificationBuilder.setContentText("Finished")
        notificationBuilder.setProgress(0, 0, false)
        notificationManager.notify(SERVICE_ID, notificationBuilder.build())
    }

    private fun getServiceNotification(): Notification.Builder {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Simulation")
            .setContentText("Ongoing fake gps ...")
            .setSmallIcon(R.drawable.move_location)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Fake locations simulation channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    class ServiceCmdReceiver(val onSimulationExtraEventListener: (String) -> Unit) :
        BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.extras?.getString("simulation")?.let {
                onSimulationExtraEventListener(it)
            }
            context.sendBroadcast(Intent("cmd_pong"))
        }
    }
}