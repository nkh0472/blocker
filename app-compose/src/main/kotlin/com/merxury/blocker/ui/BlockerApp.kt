/*
 * Copyright 2023 Blocker
 * Copyright 2022 The Android Open Source Project
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

package com.merxury.blocker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration.Long
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigation.suite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigation.suite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigation.suite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.DpSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.merxury.blocker.R
import com.merxury.blocker.core.data.util.NetworkMonitor
import com.merxury.blocker.core.data.util.PermissionMonitor
import com.merxury.blocker.core.data.util.PermissionStatus.NO_PERMISSION
import com.merxury.blocker.core.data.util.PermissionStatus.SHELL_USER
import com.merxury.blocker.core.designsystem.component.BlockerBackground
import com.merxury.blocker.core.designsystem.component.BlockerGradientBackground
import com.merxury.blocker.core.designsystem.icon.Icon.DrawableResourceIcon
import com.merxury.blocker.core.designsystem.icon.Icon.ImageVectorIcon
import com.merxury.blocker.core.model.data.IconBasedThemingState
import com.merxury.blocker.navigation.BlockerNavHost
import com.merxury.blocker.navigation.TopLevelDestination

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialNavigationApi::class,
    ExperimentalMaterial3AdaptiveNavigationSuiteApi::class,
)
@Composable
fun BlockerApp(
    windowSize: DpSize,
    networkMonitor: NetworkMonitor,
    permissionMonitor: PermissionMonitor,
    appState: BlockerAppState = rememberBlockerAppState(
        networkMonitor = networkMonitor,
        permissionMonitor = permissionMonitor,
        windowSize = windowSize,
    ),
    updateIconBasedThemingState: (IconBasedThemingState) -> Unit = {},
) {
    BlockerBackground {
        BlockerGradientBackground {
            val snackbarHostState = remember { SnackbarHostState() }
            val appPermission by appState.currentPermission.collectAsStateWithLifecycle()
            val noPermissionHint = stringResource(R.string.no_permission_hint)
            val shellPermissionHint = stringResource(R.string.shell_permission_hint)
            val actionLabel = stringResource(R.string.close)
            LaunchedEffect(appPermission) {
                if (appPermission == NO_PERMISSION) {
                    snackbarHostState.showSnackbar(
                        message = noPermissionHint,
                        actionLabel = actionLabel,
                        duration = Long,
                    )
                } else if (appPermission == SHELL_USER) {
                    snackbarHostState.showSnackbar(
                        message = shellPermissionHint,
                        actionLabel = actionLabel,
                        duration = Long,
                    )
                }
            }
            val currentDestination = appState.currentDestination
            NavigationSuiteScaffold(
                layoutType = appState.navigationSuiteType,
                modifier = Modifier.semantics {
                    testTagsAsResourceId = true
                },
                contentColor = Color.Transparent,
                navigationSuiteColors = NavigationSuiteDefaults.colors(
                    navigationBarContainerColor = Color.Transparent,
                    navigationRailContainerColor = Color.Transparent,
                    navigationDrawerContainerColor = Color.Transparent,
                ),
                navigationSuiteItems = {
                    appState.topLevelDestinations.forEach { destination ->
                        val isSelected =
                            currentDestination.isTopLevelDestinationInHierarchy(destination)
                        item(
                            selected = isSelected,
                            icon = {
                                val icon = if (isSelected) {
                                    destination.selectedIcon
                                } else {
                                    destination.unselectedIcon
                                }
                                when (icon) {
                                    is ImageVectorIcon -> Icon(
                                        imageVector = icon.imageVector,
                                        contentDescription = null,
                                    )

                                    is DrawableResourceIcon -> Icon(
                                        painter = painterResource(id = icon.id),
                                        contentDescription = null,
                                    )
                                }
                            },
                            label = { Text(stringResource(destination.iconTextId)) },
                            onClick = { appState.navigateToTopLevelDestination(destination) },
                        )
                    }
                },
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal,
                            ),
                        ),
                ) {
                    // TODO Show the top app bar on top level destinations.

                    BlockerNavHost(
                        bottomSheetNavigator = appState.bottomSheetNavigator,
                        navController = appState.navController,
                        onBackClick = appState::onBackClick,
                        dismissBottomSheet = appState::dismissBottomSheet,
                        snackbarHostState = snackbarHostState,
                        updateIconBasedThemingState = updateIconBasedThemingState,
                    )

                    // TODO: We may want to add padding or spacer when the snackbar is shown so that
                    //  content doesn't display behind it.
                }
            }
        }
    }
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false
