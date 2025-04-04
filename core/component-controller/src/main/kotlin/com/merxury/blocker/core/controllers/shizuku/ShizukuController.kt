/*
 * Copyright 2025 Blocker
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

package com.merxury.blocker.core.controllers.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.pm.IPackageManager
import android.content.pm.PackageManager
import android.os.Build
import com.merxury.blocker.core.controllers.IController
import com.merxury.blocker.core.controllers.utils.ContextUtils.userId
import com.merxury.blocker.core.model.data.ComponentInfo
import com.merxury.blocker.core.utils.ApplicationUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ShizukuController @Inject constructor(
    @ApplicationContext private val context: Context,
) : IController {
    private val pm: IPackageManager by lazy {
        IPackageManager.Stub.asInterface(
            ShizukuBinderWrapper(
                SystemServiceHelper.getSystemService("package"),
            ),
        )
    }

    override suspend fun switchComponent(
        component: ComponentInfo,
        state: Int,
    ): Boolean {
        val packageName = component.packageName
        val componentName = component.name
        // 0 means kill the application
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            pm.setComponentEnabledSetting(
                ComponentName(packageName, componentName),
                state,
                0,
                context.userId,
                context.packageName,
            )
        } else {
            pm.setComponentEnabledSetting(
                ComponentName(packageName, componentName),
                state,
                0,
                context.userId,
            )
        }
        return true
    }

    override suspend fun enable(component: ComponentInfo): Boolean = switchComponent(
        component,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
    )

    override suspend fun disable(component: ComponentInfo): Boolean = switchComponent(
        component,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
    )

    override suspend fun batchEnable(
        componentList: List<ComponentInfo>,
        action: suspend (info: ComponentInfo) -> Unit,
    ): Int {
        var successCount = 0
        componentList.forEach {
            if (enable(it)) {
                successCount++
            }
            action(it)
        }
        return successCount
    }

    override suspend fun batchDisable(
        componentList: List<ComponentInfo>,
        action: suspend (info: ComponentInfo) -> Unit,
    ): Int {
        var successCount = 0
        componentList.forEach {
            if (disable(it)) {
                successCount++
            }
            action(it)
        }
        return successCount
    }

    override suspend fun checkComponentEnableState(
        packageName: String,
        componentName: String,
    ): Boolean = ApplicationUtil.checkComponentIsEnabled(
        context.packageManager,
        ComponentName(packageName, componentName),
    )
}
