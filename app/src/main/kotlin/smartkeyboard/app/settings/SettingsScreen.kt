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

import androidx.compose.runtime.Composable
import dev.patrickgold.jetpref.datastore.ui.Preference
import smartkeyboard.R
import smartkeyboard.app.LocalNavController
import smartkeyboard.app.Routes
import smartkeyboard.lib.compose.FlorisScreen
import smartkeyboard.lib.compose.stringRes

@Composable
fun SettingsScreen() = FlorisScreen {
    title = stringRes(R.string.settings__title)
    previewFieldVisible = true

    val navController = LocalNavController.current

    content {
        Preference(
            iconId = R.drawable.ic_palette,
            title = stringRes(R.string.settings__theme__title),
            onClick = { navController.navigate(Routes.Settings.Theme) },
        )
        Preference(
            iconId = R.drawable.ic_keyboard,
            title = stringRes(R.string.settings__keyboard__title),
            onClick = { navController.navigate(Routes.Settings.Keyboard) },
        )
        Preference(
            iconId = R.drawable.ic_smartbar,
            title = stringRes(R.string.settings__smartbar__title),
            onClick = { navController.navigate(Routes.Settings.Smartbar) },
        )
        Preference(
            iconId = R.drawable.ic_spellcheck,
            title = stringRes(R.string.settings__typing__title),
            onClick = { navController.navigate(Routes.Settings.Typing) },
        )
        Preference(
            iconId = R.drawable.ic_library_books,
            title = stringRes(R.string.settings__dictionary__title),
            onClick = { navController.navigate(Routes.Settings.Dictionary) },
        )
        Preference(
            iconId = R.drawable.ic_gesture,
            title = stringRes(R.string.settings__gestures__title),
            onClick = { navController.navigate(Routes.Settings.Gestures) },
        )
        Preference(
            iconId = R.drawable.ic_assignment,
            title = stringRes(R.string.settings__clipboard__title),
            onClick = { navController.navigate(Routes.Settings.Clipboard) },
        )
        Preference(
            iconId = R.drawable.ic_sentiment_satisfied,
            title = stringRes(R.string.settings__media__title),
            onClick = { navController.navigate(Routes.Settings.Media) },
        )
        Preference(
            iconId = R.drawable.ic_adb,
            title = stringRes(R.string.devtools__title),
            onClick = { navController.navigate(Routes.Devtools.Home) },
        )
        Preference(
            iconId = R.drawable.ic_build,
            title = stringRes(R.string.settings__advanced__title),
            onClick = { navController.navigate(Routes.Settings.Advanced) },
        )
    }
}
