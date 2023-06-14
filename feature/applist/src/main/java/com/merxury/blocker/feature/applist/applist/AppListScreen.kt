/*
 * Copyright 2023 Blocker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.merxury.blocker.feature.applist.applist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.merxury.blocker.core.designsystem.component.BlockerTopAppBar
import com.merxury.blocker.core.designsystem.icon.BlockerIcons
import com.merxury.blocker.core.ui.TrackScreenViewEvent
import com.merxury.blocker.core.ui.applist.AppList
import com.merxury.blocker.core.ui.applist.model.AppItem
import com.merxury.blocker.core.ui.screen.ErrorScreen
import com.merxury.blocker.core.ui.screen.InitializingScreen
import com.merxury.blocker.feature.applist.R.string
import com.merxury.blocker.feature.applist.applist.AppListUiState.Error
import com.merxury.blocker.feature.applist.applist.AppListUiState.Initializing
import com.merxury.blocker.feature.applist.applist.AppListUiState.Success
import com.merxury.blocker.feature.applist.applist.component.TopAppBarMoreMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    uiState: AppListUiState,
    appList: List<AppItem>,
    onAppItemClick: (String) -> Unit,
    onClearCacheClick: (String) -> Unit,
    onClearDataClick: (String) -> Unit,
    onForceStopClick: (String) -> Unit,
    onUninstallClick: (String) -> Unit,
    onEnableClick: (String) -> Unit,
    onDisableClick: (String) -> Unit,
    onServiceStateUpdate: (String, Int) -> Unit,
    navigateTooAppSortScreen: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToSupportAndFeedback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            BlockerTopAppBar(
                title = stringResource(id = string.app_name),
                actions = {
                    IconButton(onClick = navigateTooAppSortScreen) {
                        Icon(
                            imageVector = BlockerIcons.Sort,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    TopAppBarMoreMenu(
                        navigateToSettings = navigateToSettings,
                        navigateToFeedback = navigateToSupportAndFeedback,
                    )
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal,
                    ),
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val appListTestTag = "appList:applicationList"
            when (uiState) {
                is Initializing -> InitializingScreen(processingName = uiState.processingName)

                is Success -> AppList(
                    appList = appList,
                    selectedAppPackageName = uiState.selectedAppPackageName,
                    onAppItemClick = onAppItemClick,
                    onClearCacheClick = onClearCacheClick,
                    onClearDataClick = onClearDataClick,
                    onForceStopClick = onForceStopClick,
                    onUninstallClick = onUninstallClick,
                    onEnableClick = onEnableClick,
                    onDisableClick = onDisableClick,
                    onServiceStateUpdate = onServiceStateUpdate,
                    modifier = modifier.testTag(appListTestTag),
                )

                is Error -> ErrorScreen(uiState.error)
            }
        }
    }
    TrackScreenViewEvent(screenName = "AppListScreen")
}
