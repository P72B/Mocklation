package de.p72b.mocklation.main.mode

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import de.p72b.mocklation.BuildConfig
import de.p72b.mocklation.R
import de.p72b.mocklation.service.location.MockLocationService
import de.p72b.mocklation.service.setting.ISetting
import de.p72b.mocklation.util.AppUtil
import de.p72b.mocklation.util.Logger
import java.util.*

class MockServiceInteractor constructor(private val activity: Activity,
                                                 private val setting: ISetting,
                                                 private val listener: MockServiceListener?) {

    companion object {
        internal val PERMISSIONS_MOCKING = 115
        internal val SERVICE_STATE_STOP = 0
        internal val SERVICE_STATE_RUNNING = 1
        internal val SERVICE_STATE_PAUSE = 2

        private val TAG = MockServiceInteractor::class.java.simpleName
    }

    private var runningServices: MutableList<Class<*>> = ArrayList()
    private val context: Context = activity.applicationContext
    private var locationItemCode: String? = null
    private var state: Int = 0
    private val localAppBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Logger.d(TAG, "Received local broadcast: " + AppUtil.toString(intent))
            val action = intent.action ?: return
            when (action) {
                MockLocationService.EVENT_PAUSE -> {
                    Logger.d(TAG, "Pause service")
                    state = SERVICE_STATE_PAUSE
                }
                MockLocationService.EVENT_PLAY -> {
                    Logger.d(TAG, "Play service")
                    state = SERVICE_STATE_RUNNING
                }
                MockLocationService.EVENT_STOP -> {
                    Logger.d(TAG, "Stop service")
                    state = SERVICE_STATE_STOP
                    runningServices.remove(MockLocationService::class.java)
                }
            }
            listener?.onUpdate()
        }
    }

    private//if marshmallow
    // in marshmallow this will always return true
    val isMockLocationEnabled: Boolean
        get() {
            var isMockLocation = false
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val opsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                    if (opsManager != null) {
                        isMockLocation = opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID) == AppOpsManager.MODE_ALLOWED
                    }
                } else {
                    isMockLocation = android.provider.Settings.Secure.getString(context.contentResolver, "mock_location") != "0"
                }
            } catch (e: Exception) {
                return false
            }

            return isMockLocation
        }

    init {
        state = if (isServiceRunning()) SERVICE_STATE_RUNNING else SERVICE_STATE_STOP
        AppUtil.registerLocalBroadcastReceiver(
                context,
                localAppBroadcastReceiver,
                MockLocationService.EVENT_PAUSE,
                MockLocationService.EVENT_PLAY,
                MockLocationService.EVENT_STOP)
    }

    fun onMockPermissionsResult(grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkDefaultMockLocationApp()
        } else {
            // TODO: permission is needed.
        }
    }

    fun pauseMockLocationService() {
        if (isServiceRunning()) {
            AppUtil.sendLocalBroadcast(context, Intent(
                    MockLocationService.EVENT_PAUSE))
            state = SERVICE_STATE_PAUSE
        }
    }

    fun playMockLocationService() {
        if (isServiceRunning()) {
            AppUtil.sendLocalBroadcast(context, Intent(
                    MockLocationService.EVENT_PLAY))
            state = SERVICE_STATE_RUNNING
        }
    }

    fun stopMockLocationService() {
        if (isServiceRunning()) {
            stopMockLocationService(MockLocationService::class.java)
        }
    }

    fun isServiceRunning(): Boolean {
        return isServiceRunning(MockLocationService::class.java)
    }

    fun startMockLocation(code: String) {
        locationItemCode = code
        Logger.d(TAG, "startMockLocation")
        val permissionsToBeRequired = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        var shouldRequestPermission = false

        for (permission in permissionsToBeRequired) {
            if (!hasPermission(permission)) {
                Logger.d(TAG, "$permission not granted.")
                shouldRequestPermission = true
                break
            }
        }

        if (shouldRequestPermission) {
            Logger.d(TAG, "Some permissions aren't granted.")

            /*// Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsToBeRequired)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

            }
            */
            ActivityCompat.requestPermissions(
                    activity,
                    permissionsToBeRequired,
                    PERMISSIONS_MOCKING)
        } else {
            Logger.d(TAG, "All permissions are granted.")
            checkDefaultMockLocationApp()
        }
    }

    fun getState(): Int {
        return state
    }

    private fun startMockLocationService(service: Class<*>) {
        if (locationItemCode == null) {
            return
        }

        if (isServiceRunning(service)) {
            Logger.d(TAG, service.simpleName + " is already running")
            return
        }


        runningServices.add(service)
        Logger.d(TAG, "START: " + service.simpleName)
        val intent = Intent(context, service)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        setting.mockLocationItemCode = locationItemCode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(intent)
        } else {
            activity.startService(intent)
        }
        state = SERVICE_STATE_RUNNING
        listener?.onStart()
    }

    private fun stopMockLocationService(service: Class<*>) {
        if (!isServiceRunning(service)) {
            Logger.d(TAG, service.simpleName + " not running")
            return
        }

        Logger.d(TAG, "STOP: " + service.simpleName)
        activity.stopService(Intent(context, service))

        if (runningServices.contains(service)) {
            runningServices.remove(service)
        }
        state = SERVICE_STATE_STOP
        listener?.onStop()

    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun checkDefaultMockLocationApp() {
        Logger.d(TAG, "checkDefaultMockLocationApp")
        if (isMockLocationEnabled) {
            Logger.d(TAG, "MockLocations is enabled APP for Mocklation")
            startMockLocationService(MockLocationService::class.java)
        } else {
            // TODO: tutorial how to enable default permission app.
            try {
                activity.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            } catch (activityNotFound: ActivityNotFoundException) {
                Toast.makeText(context, R.string.error_1019, Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    interface MockServiceListener {
        fun onStart()

        fun onStop()

        fun onUpdate()
    }
}
