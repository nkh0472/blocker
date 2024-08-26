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

package com.merxury.blocker.feature.licenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merxury.blocker.core.data.respository.licenses.LicensesRepository
import com.merxury.blocker.core.dispatchers.BlockerDispatchers.IO
import com.merxury.blocker.core.dispatchers.Dispatcher
import com.merxury.blocker.core.model.data.LicenseGroup
import com.merxury.blocker.core.model.licenses.LicenseItem
import com.merxury.blocker.feature.licenses.LicensesUiState.Loading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LicensesViewModel @Inject constructor(
    licensesRepository: LicensesRepository,
) : ViewModel() {
    val licensesUiState: StateFlow<LicensesUiState> = licensesRepository.getLicensesList()
        .map {
            // Split to groups according to groupId
            val groups = it.groupBy { licenseItem -> licenseItem.groupId }
                .map { (groupId, licenseItems) ->
                    LicenseGroup(
                        id = groupId,
                        artifacts = licenseItems.map { licenseItem ->
                            LicenseItem(
                                groupId = licenseItem.groupId,
                                artifactId = licenseItem.artifactId,
                                version = licenseItem.version,
                                spdxLicenses = licenseItem.spdxLicenses,
                                name = licenseItem.name,
                                scm = licenseItem.scm,
                            )
                        },
                    )
                }
            LicensesUiState.Success(groups)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Loading,
        )
}

sealed interface LicensesUiState {
    data object Loading : LicensesUiState

    data class Success(
        val licenses: List<LicenseGroup> = emptyList(),
    ) : LicensesUiState
}
