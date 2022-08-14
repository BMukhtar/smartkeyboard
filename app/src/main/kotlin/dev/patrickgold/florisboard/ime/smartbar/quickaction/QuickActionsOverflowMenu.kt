/*
 * Copyright (C) 2022 Patrick Goldinger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.florisboard.ime.smartbar.quickaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import dev.patrickgold.florisboard.app.florisPreferenceModel
import dev.patrickgold.florisboard.ime.keyboard.CurrencySet
import dev.patrickgold.florisboard.ime.keyboard.FlorisImeSizing
import dev.patrickgold.florisboard.keyboardManager
import dev.patrickgold.florisboard.lib.compose.FlorisButton
import dev.patrickgold.florisboard.lib.compose.florisVerticalScroll
import dev.patrickgold.jetpref.datastore.model.observeAsState

@Composable
fun QuickActionsOverflowMenu() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(FlorisImeSizing.keyboardRowBaseHeight * 4),
    ) {
        val prefs by florisPreferenceModel()
        val context = LocalContext.current
        val keyboardManager by context.keyboardManager()

        val actionArrangement by prefs.smartbar.actionArrangement.observeAsState()
        val evaluator by keyboardManager.activeEvaluator.collectAsState()

        val dynamicActionsCountToShow =
            actionArrangement.dynamicActions.size - keyboardManager.smartbarVisibleDynamicActionsCount
        val visibleActions = remember(actionArrangement, dynamicActionsCountToShow) {
            actionArrangement.dynamicActions.takeLast(dynamicActionsCountToShow)
        }

        LazyVerticalGrid(
            modifier = Modifier.fillMaxWidth(),
            columns = GridCells.Adaptive(FlorisImeSizing.smartbarHeight * 1.8f),
        ) {
            items(visibleActions) { action ->
                QuickActionButton(
                    action = action,
                    evaluator = evaluator,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                FlorisButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                    text = "customize_order_btn",
                )
            }
        }
    }
}
