/*
 * Copyright 2024 Blocker
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

import app.cash.licensee.LicenseeExtension
import app.cash.licensee.LicenseeTask
import app.cash.licensee.UnusedAction
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.merxury.blocker.AssetCopyTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.utils.named
import java.util.Locale

class LicenseeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("app.cash.licensee")
            extensions.configure<LicenseeExtension> {
                allow("Apache-2.0")
                allow("MIT")
                allow("BSD-3-Clause")
                allow("MPL-2.0")
                allowUrl("http://opensource.org/licenses/BSD-2-Clause")
                allowUrl("https://opensource.org/licenses/MIT")
                allowUrl("https://developer.android.com/studio/terms.html")
                unusedAction(UnusedAction.IGNORE)
            }
            val isAndroidLibrary = plugins.hasPlugin(LibraryPlugin::class.java)
            val isAndroidApplication = plugins.hasPlugin(AppPlugin::class.java)
            if (isAndroidLibrary || isAndroidApplication) {
                val androidComponents =
                    project.extensions.getByType(AndroidComponentsExtension::class.java)
                androidComponents.onVariants { variant ->
                    val capName = variant.name.replaceFirstChar { it.titlecase(Locale.ROOT) }
                    val licenseeTask = tasks.named<LicenseeTask>("licenseeAndroid$capName")
                    val copyArtifactsTask = tasks.register<AssetCopyTask>(
                        "copy${capName}LicenseeOutputToAndroidAssets",
                    ) {
                        inputFile.set(
                            layout.buildDirectory
                                .file("reports/licensee/android$capName/artifacts.json"),
                        )
                        outputFilename.set("licenses.json")

                        dependsOn(licenseeTask)
                    }

                    variant.sources.assets
                        ?.addGeneratedSourceDirectory(copyArtifactsTask, AssetCopyTask::outputDirectory)
                }
            }
        }
    }
}

