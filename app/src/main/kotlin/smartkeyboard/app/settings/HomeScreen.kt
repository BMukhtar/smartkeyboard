/*
 * Copyright (C) 2021 Patrick Goldinger
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

package smartkeyboard.app.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.patrickgold.jetpref.datastore.ui.Preference
import smartkeyboard.R
import smartkeyboard.app.LocalNavController
import smartkeyboard.app.Routes
import smartkeyboard.lib.compose.FlorisErrorCard
import smartkeyboard.lib.compose.FlorisScreen
import smartkeyboard.lib.compose.FlorisWarningCard
import smartkeyboard.lib.compose.stringRes
import smartkeyboard.lib.util.InputMethodUtils

@Composable
fun HomeScreen() = FlorisScreen {
    title = stringRes(R.string.settings__home__title)
    navigationIconVisible = false
    previewFieldVisible = true

    val navController = LocalNavController.current
    val context = LocalContext.current

    content {
        val isSmartKeyboardEnabled by InputMethodUtils.observeIsFlorisboardEnabled(foregroundOnly = true)
        val isSmartKeyboardSelected by InputMethodUtils.observeIsFlorisboardSelected(foregroundOnly = true)
        if (!isSmartKeyboardEnabled) {
            FlorisErrorCard(
                modifier = Modifier.padding(8.dp),
                showIcon = false,
                text = stringRes(R.string.settings__home__ime_not_enabled),
                onClick = { InputMethodUtils.showImeEnablerActivity(context) },
            )
        } else if (!isSmartKeyboardSelected) {
            FlorisWarningCard(
                modifier = Modifier.padding(8.dp),
                showIcon = false,
                text = stringRes(R.string.settings__home__ime_not_selected),
                onClick = { InputMethodUtils.showImePicker(context) },
            )
        }
        Preference(
            iconId = R.drawable.ic_language,
            title = stringRes(R.string.settings__localization__title),
            onClick = { navController.navigate(Routes.Settings.Localization) },
        )
        Preference(
            iconId = R.drawable.ic_build,
            title = stringRes(R.string.settings__title),
            onClick = { navController.navigate(Routes.Settings.MoreSettings) },
        )
        Preference(
            iconId = R.drawable.ic_info,
            title = stringRes(R.string.about__title),
            onClick = { navController.navigate(Routes.Settings.About) },
        )
    }
}
