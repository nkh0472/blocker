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

package com.merxury.blocker.ui.twopane.rule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.merxury.blocker.feature.generalrules.navigation.GeneralRuleRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class RuleList2PaneViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val generalRuleRoute: GeneralRuleRoute = savedStateHandle.toRoute()
    private val selectedRuleIdKey = "selectedRuleIdKey"
    val selectedRuleId: StateFlow<String?> = savedStateHandle.getStateFlow(
        key = selectedRuleIdKey,
        initialValue = generalRuleRoute.initialRuleId,
    )

    fun onRuleClick(ruleId: String?) {
        savedStateHandle[selectedRuleIdKey] = ruleId
    }
}
