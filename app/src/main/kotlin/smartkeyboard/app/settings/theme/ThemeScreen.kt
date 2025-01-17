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

package smartkeyboard.app.settings.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.datastore.ui.ListPreference
import dev.patrickgold.jetpref.datastore.ui.Preference
import dev.patrickgold.jetpref.datastore.ui.PreferenceGroup
import dev.patrickgold.jetpref.datastore.ui.SwitchPreference
import smartkeyboard.R
import smartkeyboard.app.LocalNavController
import smartkeyboard.app.Routes
import smartkeyboard.ime.theme.ThemeMode
import smartkeyboard.lib.compose.FlorisScreen
import smartkeyboard.lib.compose.stringRes

@Composable
fun ThemeScreen() = FlorisScreen {
    title = stringRes(R.string.settings__theme__title)
    previewFieldVisible = true

    val navController = LocalNavController.current

    content {
        val themeMode by prefs.theme.mode.observeAsState()
        val dayThemeId by prefs.theme.dayThemeId.observeAsState()
        val nightThemeId by prefs.theme.nightThemeId.observeAsState()

        ListPreference(
            prefs.theme.mode,
            iconId = R.drawable.ic_brightness_auto,
            title = stringRes(R.string.pref__theme__mode__label),
            entries = ThemeMode.listEntries(),
        )
        Preference(
            iconId = R.drawable.ic_palette,
            title = stringRes(R.string.settings__theme_manager__title_manage),
            onClick = {
                navController.navigate(Routes.Settings.ThemeManager(ThemeManagerScreenAction.MANAGE))
            },
        )

        PreferenceGroup(
            title = stringRes(R.string.pref__theme__day),
            enabledIf = { prefs.theme.mode isNotEqualTo ThemeMode.ALWAYS_NIGHT },
        ) {
            Preference(
                iconId = R.drawable.ic_light_mode,
                title = stringRes(R.string.pref__theme__any_theme__label),
                summary = dayThemeId.toString(),
                onClick = {
                    navController.navigate(Routes.Settings.ThemeManager(ThemeManagerScreenAction.SELECT_DAY))
                },
            )
            SwitchPreference(
                prefs.theme.dayThemeAdaptToApp,
                iconId = R.drawable.ic_format_paint,
                title = stringRes(R.string.pref__theme__any_theme_adapt_to_app__label),
                summary = stringRes(R.string.pref__theme__any_theme_adapt_to_app__summary),
                visibleIf = { false },
            )
        }

        PreferenceGroup(
            title = stringRes(R.string.pref__theme__night),
            enabledIf = { prefs.theme.mode isNotEqualTo ThemeMode.ALWAYS_DAY },
        ) {
            Preference(
                iconId = R.drawable.ic_dark_mode,
                title = stringRes(R.string.pref__theme__any_theme__label),
                summary = nightThemeId.toString(),
                onClick = {
                    navController.navigate(Routes.Settings.ThemeManager(ThemeManagerScreenAction.SELECT_NIGHT))
                },
            )
            SwitchPreference(
                prefs.theme.nightThemeAdaptToApp,
                iconId = R.drawable.ic_format_paint,
                title = stringRes(R.string.pref__theme__any_theme_adapt_to_app__label),
                summary = stringRes(R.string.pref__theme__any_theme_adapt_to_app__summary),
                visibleIf = { false },
            )
        }
    }
}
