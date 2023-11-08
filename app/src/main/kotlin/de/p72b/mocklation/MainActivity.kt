package de.p72b.mocklation

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import de.p72b.mocklation.service.RequirementsService
import de.p72b.mocklation.ui.MainNavigation
import de.p72b.mocklation.ui.Navigator
import de.p72b.mocklation.ui.theme.AppTheme
import de.p72b.mocklation.util.Logger
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val navigator: Navigator by inject()
    private val requirementsService: RequirementsService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(requirementsService)

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
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Logger.d(msg = "Permission granted")
            } else {
                Logger.d(msg = "Permission NOT granted")
            }
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                Logger.d(msg = "Permission granted")
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Logger.d(msg = "Show a dialog why we need it")
            }

            else -> {
                requestPermissions(
                    getLocationForegroundServicePermissions(),
                    555
                )
            }
        }
    }

    private fun getLocationForegroundServicePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    override fun onDestroy() {
        requestPermissionLauncher.unregister()
        super.onDestroy()
    }
}