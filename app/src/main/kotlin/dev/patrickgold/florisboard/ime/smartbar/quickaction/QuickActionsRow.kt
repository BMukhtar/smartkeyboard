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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.florisPreferenceModel
import dev.patrickgold.florisboard.ime.smartbar.SmartbarLayout
import dev.patrickgold.florisboard.ime.text.keyboard.TextKeyData
import dev.patrickgold.florisboard.ime.theme.FlorisImeTheme
import dev.patrickgold.florisboard.ime.theme.FlorisImeUi
import dev.patrickgold.florisboard.keyboardManager
import dev.patrickgold.florisboard.lib.snygg.ui.snyggBackground
import dev.patrickgold.florisboard.lib.snygg.ui.snyggShadow
import dev.patrickgold.florisboard.lib.snygg.ui.solidColor
import dev.patrickgold.jetpref.datastore.model.observeAsState

private val SmartbarActionPadding = 4.dp

@Composable
fun QuickActionsRow(modifier: Modifier = Modifier) = with(LocalDensity.current) {
    val prefs by florisPreferenceModel()
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()

    val flipToggles by prefs.smartbar.flipToggles.observeAsState()
    val evaluator by keyboardManager.activeEvaluator.collectAsState()
    val smartbarLayout by prefs.smartbar.layout.observeAsState()
    val actionArrangement by prefs.smartbar.actionArrangement.observeAsState()

    val dynamicActions = remember(smartbarLayout, actionArrangement) {
        if (smartbarLayout == SmartbarLayout.ACTIONS_ONLY && actionArrangement.stickyAction != null) {
            buildList {
                add(actionArrangement.stickyAction!!)
                addAll(actionArrangement.dynamicActions)
            }
        } else {
            actionArrangement.dynamicActions
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toDp()
        val height = constraints.maxHeight.toDp()
        val numActionsToShow = ((width / height).toInt() - 1).coerceAtLeast(0)
        val visibleActions = dynamicActions
            .subList(0, numActionsToShow.coerceAtMost(dynamicActions.size))

        SideEffect {
            keyboardManager.smartbarVisibleDynamicActionsCount =
                if (smartbarLayout == SmartbarLayout.ACTIONS_ONLY && actionArrangement.stickyAction != null) {
                    numActionsToShow - 1
                } else {
                    numActionsToShow
                }.coerceAtLeast(0)
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            if (flipToggles) {
                MoreButton()
            }
            for (action in visibleActions) {
                QuickActionButton(action, evaluator)
            }
            if (!flipToggles) {
                MoreButton()
            }
        }
    }
}

@Composable
private fun MoreButton() {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val moreStyle = FlorisImeTheme.style.get(FlorisImeUi.SmartbarQuickAction)

    IconButton(
        onClick = {
            keyboardManager.inputEventDispatcher.sendDownUp(TextKeyData.TOGGLE_ACTIONS_OVERFLOW)
        },
        modifier = Modifier
            .padding(SmartbarActionPadding)
            .fillMaxHeight()
            .aspectRatio(1f),
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxHeight()
                .aspectRatio(1f)
                .snyggShadow(moreStyle)
                .snyggBackground(moreStyle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.padding(2.dp),
                painter = painterResource(R.drawable.ic_more_horiz),
                contentDescription = null,
                tint = moreStyle.foreground.solidColor(),
            )
        }
    }
}
