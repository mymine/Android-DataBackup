package com.xayah.feature.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xayah.core.ui.component.ActionButton
import com.xayah.core.ui.component.BodySmallText
import com.xayah.core.ui.component.InnerBottomSpacer
import com.xayah.core.ui.component.InnerTopSpacer
import com.xayah.core.ui.component.LabelLargeText
import com.xayah.core.ui.material3.toColor
import com.xayah.core.ui.material3.tokens.ColorSchemeKeyTokens
import com.xayah.core.ui.model.ImageVectorToken
import com.xayah.core.ui.model.StringResourceToken
import com.xayah.core.ui.token.SizeTokens
import com.xayah.core.ui.util.fromVector
import com.xayah.core.ui.util.value

@Composable
fun SetupScaffold(topBar: @Composable () -> Unit = {}, actions: @Composable RowScope.() -> Unit, content: @Composable LazyItemScope.() -> Unit) {
    Scaffold(
        containerColor = ColorSchemeKeyTokens.Surface.toColor(),
        topBar = topBar,
    ) { innerPadding ->
        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        InnerTopSpacer(innerPadding = innerPadding)
                    }
                    item {
                        content()
                    }

                    item {
                        InnerBottomSpacer(innerPadding = innerPadding)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SizeTokens.Level16),
                horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level12, Alignment.End),
            ) {
                actions()
            }

            InnerBottomSpacer(innerPadding = innerPadding)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun PermissionButton(title: StringResourceToken, desc: StringResourceToken, envState: EnvState, onClick: () -> Unit) {
    ActionButton(
        icon = envState.icon,
        colorContainer = envState.colorContainer,
        colorL80D20 = envState.colorL80D20,
        onColorContainer = envState.onColorContainer,
        actionIcon = ImageVectorToken.fromVector(Icons.Rounded.KeyboardArrowRight),
        onClick = onClick
    ) {
        Column(modifier = Modifier.weight(1f)) {
            LabelLargeText(text = title.value, color = ColorSchemeKeyTokens.OnSurface.toColor())
            BodySmallText(text = desc.value, color = ColorSchemeKeyTokens.OnSurfaceVariant.toColor())
        }
    }
}
