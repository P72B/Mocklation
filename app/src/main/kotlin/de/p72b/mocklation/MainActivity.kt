package de.p72b.mocklation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import de.p72b.mocklation.service.RequirementsService
import de.p72b.mocklation.ui.MainNavigation
import de.p72b.mocklation.ui.Navigator
import de.p72b.mocklation.ui.model.collection.CollectionViewModel
import de.p72b.mocklation.ui.model.requirements.Action
import de.p72b.mocklation.ui.model.requirements.PermissionRequest
import de.p72b.mocklation.ui.model.requirements.RequirementsViewModel
import de.p72b.mocklation.ui.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private val navigator: Navigator by inject()
    private val requirementsService: RequirementsService by inject()
    private val requirementsViewModel: RequirementsViewModel by inject()
    private val collectionViewModel: CollectionViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(requirementsService)
        lifecycle.addObserver(collectionViewModel)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(rememberNavController(), navigator)
                }
            }
        }

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach {
                when (it.key) {
                    android.Manifest.permission.ACCESS_FINE_LOCATION -> {
                        PermissionUtil.firstTimeAskingPermission(
                            applicationContext,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            false
                        )
                        requirementsService.foregroundFineLocationPermissionEnabled = it.value
                    }

                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            return@forEach
                        }
                        PermissionUtil.firstTimeAskingPermission(
                            applicationContext,
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            false
                        )
                        requirementsService.backgroundLocationPermissionEnabled = it.value
                    }

                    android.Manifest.permission.POST_NOTIFICATIONS -> {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            return@forEach
                        }
                        PermissionUtil.firstTimeAskingPermission(
                            applicationContext,
                            android.Manifest.permission.POST_NOTIFICATIONS,
                            false
                        )
                        requirementsService.isAllowedToShowNotification = it.value
                    }
                }
            }

        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                requirementsViewModel.action.collect { target ->
                    when (target) {
                        Action.OpenAppSettings -> openAppSystemSettings()
                        Action.OpenDeveloperSettings -> openDeveloperSettings()
                        Action.OpenAboutPhoneSettings -> openAboutPhoneSettings()
                        null -> {
                            // nothing to do here
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                requirementsViewModel.requestPermission.collect { permission ->
                    when (permission) {
                        is PermissionRequest.PermissionBackgroundLocation -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                requestPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                    )
                                )
                            }
                        }

                        is PermissionRequest.PermissionFineLocation -> {
                            requestPermissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }

                        is PermissionRequest.PermissionNotification -> {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                return@collect
                            }
                            requestPermissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.POST_NOTIFICATIONS
                                )
                            )
                        }

                        null -> {
                            // initial state nothing should happen
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requirementsService.isAllowedToShowNotification =
                NotificationManagerCompat.from(application)
                    .areNotificationsEnabled()
        } else {
            requirementsService.isAllowedToShowNotification = true
        }

        requirementsService.foregroundFineLocationPermissionEnabled =
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requirementsService.backgroundLocationPermissionEnabled =
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

            if (PermissionUtil.isFirstTimeAskingPermission(
                    this,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            ) {
                requirementsService.shouldShowDialogRequestBackgroundLocationPermissionRationale =
                    false
            } else {
                if (requirementsService.backgroundLocationPermissionEnabled.not()) {
                    requirementsService.shouldShowDialogRequestBackgroundLocationPermissionRationale =
                        true
                } else {
                    requirementsService.shouldShowDialogRequestBackgroundLocationPermissionRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                }
            }
        }

        if (PermissionUtil.isFirstTimeAskingPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            requirementsService.shouldShowDialogRequestLocationPermissionRationale = false
        } else {
            if (requirementsService.foregroundFineLocationPermissionEnabled.not()) {
                requirementsService.shouldShowDialogRequestLocationPermissionRationale = true
            } else {
                requirementsService.shouldShowDialogRequestLocationPermissionRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (PermissionUtil.isFirstTimeAskingPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                requirementsService.shouldShowDialogRequestNotificationPermissionRationale = false
            } else {
                if (NotificationManagerCompat.from(application).areNotificationsEnabled().not()) {
                    requirementsService.shouldShowDialogRequestNotificationPermissionRationale =
                        true
                } else {
                    requirementsService.shouldShowDialogRequestNotificationPermissionRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        )
                }
            }

        }
    }

    override fun onDestroy() {
        requestPermissionLauncher.unregister()
        super.onDestroy()
    }

    private fun openAppSystemSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
        })
    }

    private fun openDeveloperSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
    }

    private fun openAboutPhoneSettings() {
        startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))
    }

    object PermissionUtil {
        private const val PREFS_FILE_NAME = "preference"

        fun firstTimeAskingPermission(context: Context, permission: String, isFirstTime: Boolean) {
            val sharedPreference = context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE)
            sharedPreference.edit().putBoolean(
                permission,
                isFirstTime
            ).apply()
        }

        fun isFirstTimeAskingPermission(context: Context, permission: String): Boolean {
            val sharedPreference = context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE)
            return sharedPreference.getBoolean(
                permission,
                true
            )

        }
    }
}