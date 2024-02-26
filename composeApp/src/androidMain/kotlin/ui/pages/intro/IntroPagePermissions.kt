package ui.pages.intro

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnyminerz.pocketchips.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.flow.MutableStateFlow
import ui.pages.intro.IntroPagePermissions.permissionsGranted

@ExperimentalPermissionsApi
object IntroPagePermissions : IntroPage(
    image = {
        val granted by permissionsGranted.collectAsState(false)
        if (granted)
            painterResource(R.drawable.icons8_unlock)
        else
            painterResource(R.drawable.icons8_lock)
    },
    title = { stringResource(R.string.intro_permissions_title) },
    message = { stringResource(R.string.intro_permissions_message) }
) {
    val permissionsGranted = MutableStateFlow(false)

    val permissions = listOfNotNull(
        // In theory only required for SDK <= 28, but the Nearby library seems to require it
        // if (Build.VERSION.SDK_INT <= 28)
        Manifest.permission.ACCESS_COARSE_LOCATION,
        // else null,

        // In theory only required for SDK 29..31, but the Nearby library seems to require it
        // if (Build.VERSION.SDK_INT in 29..31)
        Manifest.permission.ACCESS_FINE_LOCATION,
        // else null,

        if (Build.VERSION.SDK_INT <= 30)
            Manifest.permission.BLUETOOTH
        else null,
        if (Build.VERSION.SDK_INT <= 30)
            Manifest.permission.BLUETOOTH_ADMIN
        else null,

        // In theory only required for SDK <= 31, but the Nearby library seems to require it
        // if (Build.VERSION.SDK_INT <= 31)
        Manifest.permission.ACCESS_WIFI_STATE,
        // else null,
        // if (Build.VERSION.SDK_INT <= 31)
        Manifest.permission.CHANGE_WIFI_STATE,
        // else null,

        if (Build.VERSION.SDK_INT >= 31)
            Manifest.permission.BLUETOOTH_ADVERTISE
        else null,
        if (Build.VERSION.SDK_INT >= 31)
            Manifest.permission.BLUETOOTH_CONNECT
        else null,
        if (Build.VERSION.SDK_INT >= 31)
            Manifest.permission.BLUETOOTH_SCAN
        else null,

        if (Build.VERSION.SDK_INT >= 33)
            Manifest.permission.NEARBY_WIFI_DEVICES
        else null,
    )

    @Composable
    override fun ColumnScope.Content() {
        val permissionState = rememberMultiplePermissionsState(permissions)

        LaunchedEffect(permissionState) {
            snapshotFlow { permissionState.allPermissionsGranted }
                .collect {
                    permissionsGranted.value = it
                    canGoNext.value = it
                }
        }

        PageContent()

        AnimatedVisibility(
            visible = !permissionState.allPermissionsGranted,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            OutlinedButton(
                onClick = { permissionState.launchMultiplePermissionRequest() },
                modifier = Modifier.padding(bottom = 16.dp)
            ) { Text("Ask for permissions") }
        }
    }
}
