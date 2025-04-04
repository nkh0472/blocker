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

package com.merxury.blocker.core.data.test.repository

import com.merxury.blocker.core.data.respository.app.AppRepository
import com.merxury.blocker.core.model.data.InstalledApp
import com.merxury.blocker.core.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeAppRepository @Inject constructor() : AppRepository {
    override fun getApplicationList(): Flow<List<InstalledApp>> = flowOf(emptyList())

    override fun updateApplication(packageName: String): Flow<Result<Unit>> = flowOf(Result.Success(Unit))

    override fun updateApplicationList(): Flow<Result<Unit>> = flowOf(Result.Success(Unit))

    override fun searchInstalledApplications(keyword: String): Flow<List<InstalledApp>> = flowOf(emptyList())

    override fun getApplication(packageName: String): Flow<InstalledApp?> = flowOf(null)
}
