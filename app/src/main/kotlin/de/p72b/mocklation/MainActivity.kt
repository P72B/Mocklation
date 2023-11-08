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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import de.p72b.mocklation.service.RequirementsService
import de.p72b.mocklation.ui.MainNavigation
import de.p72b.mocklation.ui.Navigator
import de.p72b.mocklation.ui.model.requirements.PermissionRequest
import de.p72b.mocklation.ui.model.requirements.RequirementsViewModel
import de.p72b.mocklation.ui.theme.AppTheme
import de.p72b.mocklation.util.Logger
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val navigator: Navigator by inject()
    private val requirementsService: RequirementsService by inject()
    private val requirementsViewModel: RequirementsViewModel by inject()

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
            requirementsService.foregroundFineLocationPermissionEnabled = isGranted
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                requirementsViewModel.requestPermission.collect { permission ->
                    when (permission) {
                        PermissionRequest.PermissionBackgroundLocation -> {
                            requestPermissions(
                                getLocationForegroundServicePermissions(),
                                555
                            )
                        }

                        PermissionRequest.PermissionFineLocation -> {
                            requestPermissions(
                                getFineLocationPermissions(),
                                556
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

        if (ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {
            requirementsService.foregroundFineLocationPermissionEnabled = true
        }
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            requirementsService.backgroundLocationPermissionEnabled = true
        }

        when {
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                requirementsService.foregroundFineLocationPermissionEnabled = false
            }
        }
    }

    private fun getLocationForegroundServicePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    private fun getFineLocationPermissions(): Array<String> {
        return arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onDestroy() {
        requestPermissionLauncher.unregister()
        super.onDestroy()
    }
}