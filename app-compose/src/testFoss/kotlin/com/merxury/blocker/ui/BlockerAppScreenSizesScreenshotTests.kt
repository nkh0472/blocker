/*
 * Copyright 2025 Blocker
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

import android.util.Log
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import androidx.window.core.layout.WindowSizeClass
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.github.takahirom.roborazzi.captureRoboImage
import com.merxury.blocker.core.data.util.NetworkMonitor
import com.merxury.blocker.core.data.util.PermissionMonitor
import com.merxury.blocker.core.data.util.TimeZoneMonitor
import com.merxury.blocker.core.designsystem.theme.BlockerTheme
import com.merxury.blocker.core.testing.util.DefaultRoborazziOptions
import com.merxury.blocker.uitesthiltmanifest.HiltComponentActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import java.util.TimeZone
import javax.inject.Inject

/**
 * Tests that the navigation UI is rendered correctly on different screen sizes.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// Configure Robolectric to use a very large screen size that can fit all of the test sizes.
// This allows enough room to render the content under test without clipping or scaling.
@Config(application = HiltTestApplication::class, qualifiers = "w1000dp-h1000dp-480dpi")
@LooperMode(LooperMode.Mode.PAUSED)
@HiltAndroidTest
class BlockerAppScreenSizesScreenshotTests {

    /**
     * Manages the components' state and is used to perform injection on your test
     */
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    /**
     * Use a test activity to set the content on.
     */
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var timeZoneMonitor: TimeZoneMonitor

    @Inject
    lateinit var permissionMonitor: PermissionMonitor

    @Before
    fun setup() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(
            InstrumentationRegistry.getInstrumentation().context,
            config,
        )

        hiltRule.inject()
    }

    @Before
    fun setTimeZone() {
        // Make time zone deterministic in tests
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    private fun testBlockerAppScreenshotWithSize(width: Dp, height: Dp, screenshotName: String) {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalInspectionMode provides true,
            ) {
                DeviceConfigurationOverride(
                    override = DeviceConfigurationOverride.ForcedSize(DpSize(width, height)),
                ) {
                    BlockerTheme {
                        val appState = rememberBlockerAppState(
                            networkMonitor = networkMonitor,
                            permissionMonitor = permissionMonitor,
                            timeZoneMonitor = timeZoneMonitor,
                        )
                        BlockerApp(
                            appState,
                            windowAdaptiveInfo = WindowAdaptiveInfo(
                                windowSizeClass = WindowSizeClass.compute(
                                    width.value,
                                    height.value,
                                ),
                                windowPosture = Posture(),
                            ),
                        )
                    }
                }
            }
        }

        composeTestRule.onRoot()
            .captureRoboImage(
                "src/testFoss/screenshots/$screenshotName.png",
                roborazziOptions = DefaultRoborazziOptions,
            )
    }

    @Test
    fun compactWidth_compactHeight_showsNavigationBar() {
        testBlockerAppScreenshotWithSize(
            610.dp,
            400.dp,
            "compactWidth_compactHeight_showsNavigationBar",
        )
    }

    @Test
    fun mediumWidth_compactHeight_showsNavigationRail() {
        testBlockerAppScreenshotWithSize(
            610.dp,
            400.dp,
            "mediumWidth_compactHeight_showsNavigationRail",
        )
    }

    @Test
    fun expandedWidth_compactHeight_showsNavigationRail() {
        testBlockerAppScreenshotWithSize(
            900.dp,
            400.dp,
            "expandedWidth_compactHeight_showsNavigationRail",
        )
    }

    @Test
    fun compactWidth_mediumHeight_showsNavigationBar() {
        testBlockerAppScreenshotWithSize(
            400.dp,
            500.dp,
            "compactWidth_mediumHeight_showsNavigationBar",
        )
    }

    @Test
    fun mediumWidth_mediumHeight_showsNavigationRail() {
        testBlockerAppScreenshotWithSize(
            610.dp,
            500.dp,
            "mediumWidth_mediumHeight_showsNavigationRail",
        )
    }

    @Test
    fun expandedWidth_mediumHeight_showsNavigationRail() {
        testBlockerAppScreenshotWithSize(
            900.dp,
            500.dp,
            "expandedWidth_mediumHeight_showsNavigationRail",
        )
    }

    @Test
    fun compactWidth_expandedHeight_showsNavigationBar() {
        testBlockerAppScreenshotWithSize(
            400.dp,
            1000.dp,
            "compactWidth_expandedHeight_showsNavigationBar",
        )
    }

    @Test
    fun mediumWidth_expandedHeight_showsNavigationRail() {
        testBlockerAppScreenshotWithSize(
            610.dp,
            1000.dp,
            "mediumWidth_expandedHeight_showsNavigationRail",
        )
    }

    @Test
    fun expandedWidth_expandedHeight_showsNavigationRail() {
        testBlockerAppScreenshotWithSize(
            900.dp,
            1000.dp,
            "expandedWidth_expandedHeight_showsNavigationRail",
        )
    }
}
