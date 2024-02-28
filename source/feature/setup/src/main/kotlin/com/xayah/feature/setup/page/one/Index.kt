package com.xayah.feature.setup.page.one

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.core.ui.component.BodyMediumText
import com.xayah.core.ui.component.HeadlineMediumText
import com.xayah.core.ui.component.Section
import com.xayah.core.ui.component.SetOnResume
import com.xayah.core.ui.component.paddingTop
import com.xayah.core.ui.material3.toColor
import com.xayah.core.ui.material3.tokens.ColorSchemeKeyTokens
import com.xayah.core.ui.model.ImageVectorToken
import com.xayah.core.ui.model.StringResourceToken
import com.xayah.core.ui.token.SizeTokens
import com.xayah.core.ui.util.LocalNavController
import com.xayah.core.ui.util.fromDrawable
import com.xayah.core.ui.util.fromString
import com.xayah.core.ui.util.fromStringId
import com.xayah.core.ui.util.value
import com.xayah.feature.setup.PermissionButton
import com.xayah.feature.setup.R
import com.xayah.feature.setup.SetupRoutes
import com.xayah.feature.setup.SetupScaffold

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
fun PageOne() {
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val viewModel = hiltViewModel<IndexViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rootState by viewModel.rootState.collectAsStateWithLifecycle()
    val abiState by viewModel.abiState.collectAsStateWithLifecycle()
    val notificationState by viewModel.notificationState.collectAsStateWithLifecycle()
    val allRequiredValidated by viewModel.allRequiredValidated.collectAsStateWithLifecycle()
    val allOptionalValidated by viewModel.allOptionalValidated.collectAsStateWithLifecycle()

    SetOnResume {
        viewModel.emitIntent(IndexUiIntent.OnResume)
    }

    SetupScaffold(
        actions = {
            OutlinedButton(
                enabled = allRequiredValidated.not() || allOptionalValidated.not(),
                onClick = {
                    viewModel.launchOnIO {
                        viewModel.suspendEmitIntent(IndexUiIntent.ValidateRoot)
                        viewModel.suspendEmitIntent(IndexUiIntent.ValidateAbi)
                        viewModel.suspendEmitIntent(IndexUiIntent.ValidateNotification(context = context))
                    }
                }
            ) {
                Text(text = StringResourceToken.fromStringId(R.string.grant_all).value)
            }
            Button(enabled = allRequiredValidated, onClick = { navController.navigate(SetupRoutes.Two.route) }) {
                Text(text = StringResourceToken.fromStringId(R.string._continue).value)
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {
            Column(
                modifier = Modifier
                    .paddingTop(SizeTokens.Level100),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(SizeTokens.Level128)
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.ic_launcher_background_tonal)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier.size(SizeTokens.Level80),
                        imageVector = ImageVectorToken.fromDrawable(R.drawable.ic_launcher_foreground_tonal).value,
                        contentDescription = null
                    )
                }
                HeadlineMediumText(modifier = Modifier.paddingTop(SizeTokens.Level12), text = StringResourceToken.fromStringId(R.string.welcome_to_use).value)
                BodyMediumText(text = StringResourceToken.fromStringId(R.string.app_short_desc).value, color = ColorSchemeKeyTokens.OnSurfaceVariant.toColor())
            }

            Spacer(modifier = Modifier.size(SizeTokens.Level24))

            Section(title = StringResourceToken.fromStringId(R.string.required)) {
                PermissionButton(
                    title = StringResourceToken.fromStringId(R.string.root_permission),
                    desc = StringResourceToken.fromStringId(R.string.root_permission_desc),
                    envState = rootState
                ) {
                    viewModel.launchOnIO {
                        viewModel.suspendEmitIntent(IndexUiIntent.ValidateRoot)
                    }
                }
                PermissionButton(
                    title = StringResourceToken.fromStringId(R.string.abi_validation),
                    desc = StringResourceToken.fromString(uiState.abiErr.ifEmpty { context.getString(R.string.abi_validation_desc) }),
                    envState = abiState,
                ) {
                    viewModel.launchOnIO {
                        viewModel.suspendEmitIntent(IndexUiIntent.ValidateAbi)
                    }
                }
            }

            Section(title = StringResourceToken.fromStringId(R.string.optional)) {
                PermissionButton(
                    title = StringResourceToken.fromStringId(R.string.notification_permission),
                    desc = StringResourceToken.fromStringId(R.string.notification_permission_desc),
                    envState = notificationState,
                ) {
                    viewModel.launchOnIO {
                        viewModel.suspendEmitIntent(IndexUiIntent.ValidateNotification(context = context))
                    }
                }
            }
        }
    }
}
