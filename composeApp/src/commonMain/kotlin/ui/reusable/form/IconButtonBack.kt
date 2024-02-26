package ui.reusable.form

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import pocketchips.composeapp.generated.resources.Res

@Composable
fun IconButtonBack(onBackRequested: () -> Unit) {
    IconButton(
        onClick = onBackRequested
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            stringResource(Res.string.back)
        )
    }
}
