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
import androidx.lifecycle.lifecycleScope
import de.p72b.mocklation.MainActivity
import de.p72b.mocklation.R
import de.p72b.mocklation.data.Feature
import de.p72b.mocklation.data.PreferencesRepository
import de.p72b.mocklation.data.util.Status
import de.p72b.mocklation.service.location.LocationSimulation
import de.p72b.mocklation.ui.model.collection.CollectionUIState
import de.p72b.mocklation.usecase.GetFeatureUseCase
import de.p72b.mocklation.util.Logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject


class ForegroundService : Service() {

    private val getFeatureUseCase: GetFeatureUseCase by inject()
    private val preferencesRepository: PreferencesRepository by inject()

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "location simulation"
        private const val SERVICE_ID = 72
    }

    private var startMode: Int = 0
    private val echoReceiver = ServiceEchoReceiver()
    private lateinit var simulation: LocationSimulation
    private lateinit var feature: Feature

    override fun onCreate() {
        Logger.d(msg = "ForegroundService onCreate")
        ContextCompat.registerReceiver(
            applicationContext,
            echoReceiver,
            IntentFilter("ping"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        ContextCompat.registerReceiver(
            applicationContext,
            echoReceiver,
            IntentFilter("cmd"),
            ContextCompat.RECEIVER_NOT_EXPORTED
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
                            startForeground(SERVICE_ID, getServiceNotification())
                        }

                        Status.ERROR -> stopSelf()
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d(msg = "ForegroundService onStartCommand")
        simulation = LocationSimulation(applicationContext, feature)
        simulation.run()
        return startMode
    }

    override fun onBind(intent: Intent): IBinder? {
        Logger.d(msg = "ForegroundService onBind")
        return null
    }

    override fun onDestroy() {
        simulation.stop()
        applicationContext.unregisterReceiver(echoReceiver)
        Logger.d(msg = "ForegroundService onDestroy")
    }

    private fun getServiceNotification(): Notification {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
        return Notification.Builder(this, Companion.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Simulation")
            .setContentText("Ongoing fake gps ...")
            .setSmallIcon(R.drawable.move_location)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Fake locations simulation channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    class ServiceEchoReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Logger.d(msg = "ForegroundService ServiceEchoReceiver onReceive intent action: ${intent.action}")
            context.sendBroadcast(Intent("pong"))
        }
    }
}