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

package smartkeyboard.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import smartkeyboard.app.devtools.AndroidLocalesScreen
import smartkeyboard.app.devtools.AndroidSettingsScreen
import smartkeyboard.app.devtools.DevtoolsScreen
import smartkeyboard.app.devtools.ExportDebugLogScreen
import smartkeyboard.app.ext.ExtensionEditScreen
import smartkeyboard.app.ext.ExtensionExportScreen
import smartkeyboard.app.ext.ExtensionImportScreen
import smartkeyboard.app.ext.ExtensionImportScreenType
import smartkeyboard.app.ext.ExtensionViewScreen
import smartkeyboard.app.settings.HomeScreen
import smartkeyboard.app.settings.about.AboutScreen
import smartkeyboard.app.settings.about.ProjectLicenseScreen
import smartkeyboard.app.settings.about.ThirdPartyLicensesScreen
import smartkeyboard.app.settings.advanced.AdvancedScreen
import smartkeyboard.app.settings.advanced.BackupScreen
import smartkeyboard.app.settings.advanced.RestoreScreen
import smartkeyboard.app.settings.clipboard.ClipboardScreen
import smartkeyboard.app.settings.dictionary.DictionaryScreen
import smartkeyboard.app.settings.dictionary.UserDictionaryScreen
import smartkeyboard.app.settings.dictionary.UserDictionaryType
import smartkeyboard.app.settings.gestures.GesturesScreen
import smartkeyboard.app.settings.keyboard.InputFeedbackScreen
import smartkeyboard.app.settings.keyboard.KeyboardScreen
import smartkeyboard.app.settings.localization.LocalizationScreen
import smartkeyboard.app.settings.localization.SelectLocaleScreen
import smartkeyboard.app.settings.localization.SubtypeEditorScreen
import smartkeyboard.app.settings.media.MediaScreen
import smartkeyboard.app.settings.smartbar.SmartbarScreen
import smartkeyboard.app.settings.theme.ThemeManagerScreen
import smartkeyboard.app.settings.theme.ThemeManagerScreenAction
import smartkeyboard.app.settings.theme.ThemeScreen
import smartkeyboard.app.settings.typing.TypingScreen
import smartkeyboard.app.setup.SetupScreen
import smartkeyboard.lib.kotlin.curlyFormat

@Suppress("FunctionName")
object Routes {
    object Setup {
        const val Screen = "setup"
    }

    object Settings {
        const val Home = "settings"

        const val Localization = "settings/localization"
        const val SelectLocale = "settings/localization/select-locale"
        const val SubtypeAdd = "settings/localization/subtype/add"
        const val SubtypeEdit = "settings/localization/subtype/edit/{id}"
        fun SubtypeEdit(id: Long) = SubtypeEdit.curlyFormat("id" to id)

        const val Theme = "settings/theme"
        const val ThemeManager = "settings/theme/manage/{action}"
        fun ThemeManager(action: ThemeManagerScreenAction) = ThemeManager.curlyFormat("action" to action.id)

        const val Keyboard = "settings/keyboard"
        const val InputFeedback = "settings/keyboard/input-feedback"

        const val Smartbar = "settings/smartbar"

        const val Typing = "settings/typing"

        const val Dictionary = "settings/dictionary"
        const val UserDictionary = "settings/dictionary/user-dictionary/{type}"
        fun UserDictionary(type: UserDictionaryType) = UserDictionary.curlyFormat("type" to type.id)

        const val Gestures = "settings/gestures"

        const val Clipboard = "settings/clipboard"

        const val Media = "settings/media"

        const val Advanced = "settings/advanced"
        const val Backup = "settings/advanced/backup"
        const val Restore = "settings/advanced/restore"

        const val About = "settings/about"
        const val ProjectLicense = "settings/about/project-license"
        const val ThirdPartyLicenses = "settings/about/third-party-licenses"
    }

    object Devtools {
        const val Home = "devtools"

        const val AndroidLocales = "devtools/android/locales"
        const val AndroidSettings = "devtools/android/settings/{name}"
        fun AndroidSettings(name: String) = AndroidSettings.curlyFormat("name" to name)

        const val ExportDebugLog = "export-debug-log"
    }

    object Ext {
        const val Edit = "ext/edit/{id}?create={serial_type}"
        fun Edit(id: String, serialType: String? = null): String {
            return Edit.curlyFormat("id" to id, "serial_type" to (serialType ?: ""))
        }

        const val Export = "ext/export/{id}"
        fun Export(id: String) = Export.curlyFormat("id" to id)

        const val Import = "ext/import/{type}?uuid={uuid}"
        fun Import(
            type: ExtensionImportScreenType,
            uuid: String?,
        ) = Import.curlyFormat("type" to type.id, "uuid" to uuid.toString())

        const val View = "ext/view/{id}"
        fun View(id: String) = View.curlyFormat("id" to id)
    }

    @Composable
    fun AppNavHost(
        modifier: Modifier,
        navController: NavHostController,
        startDestination: String,
    ) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = startDestination,
        ) {
            composable(Setup.Screen) { SetupScreen() }

            composable(Settings.Home) { HomeScreen() }

            composable(Settings.Localization) { LocalizationScreen() }
            composable(Settings.SelectLocale) { SelectLocaleScreen() }
            composable(Settings.SubtypeAdd) { SubtypeEditorScreen(null) }
            composable(Settings.SubtypeEdit) { navBackStack ->
                val id = navBackStack.arguments?.getString("id")?.toLongOrNull()
                SubtypeEditorScreen(id)
            }

            composable(Settings.Theme) { ThemeScreen() }
            composable(Settings.ThemeManager) { navBackStack ->
                val action = navBackStack.arguments?.getString("action")?.let { actionId ->
                    ThemeManagerScreenAction.values().firstOrNull { it.id == actionId }
                }
                ThemeManagerScreen(action)
            }

            composable(Settings.Keyboard) { KeyboardScreen() }
            composable(Settings.InputFeedback) { InputFeedbackScreen() }

            composable(Settings.Smartbar) { SmartbarScreen() }

            composable(Settings.Typing) { TypingScreen() }

            composable(Settings.Dictionary) { DictionaryScreen() }
            composable(Settings.UserDictionary) { navBackStack ->
                val type = navBackStack.arguments?.getString("type")?.let { typeId ->
                    UserDictionaryType.values().firstOrNull { it.id == typeId }
                }
                UserDictionaryScreen(type!!)
            }

            composable(Settings.Gestures) { GesturesScreen() }

            composable(Settings.Clipboard) { ClipboardScreen() }

            composable(Settings.Media) { MediaScreen() }

            composable(Settings.Advanced) { AdvancedScreen() }
            composable(Settings.Backup) { BackupScreen() }
            composable(Settings.Restore) { RestoreScreen() }

            composable(Settings.About) { AboutScreen() }
            composable(Settings.ProjectLicense) { ProjectLicenseScreen() }
            composable(Settings.ThirdPartyLicenses) { ThirdPartyLicensesScreen() }

            composable(Devtools.Home) { DevtoolsScreen() }
            composable(Devtools.AndroidLocales) { AndroidLocalesScreen() }
            composable(Devtools.AndroidSettings) { navBackStack ->
                val name = navBackStack.arguments?.getString("name")
                AndroidSettingsScreen(name)
            }
            composable(Devtools.ExportDebugLog) { ExportDebugLogScreen() }

            composable(Ext.Edit) { navBackStack ->
                val extensionId = navBackStack.arguments?.getString("id")
                val serialType = navBackStack.arguments?.getString("serial_type")
                ExtensionEditScreen(
                    id = extensionId.toString(),
                    createSerialType = serialType.takeIf { it != null && it.isNotBlank() },
                )
            }
            composable(Ext.Export) { navBackStack ->
                val extensionId = navBackStack.arguments?.getString("id")
                ExtensionExportScreen(id = extensionId.toString())
            }
            composable(Ext.Import) { navBackStack ->
                val type = navBackStack.arguments?.getString("type")?.let { typeId ->
                    ExtensionImportScreenType.values().firstOrNull { it.id == typeId }
                } ?: ExtensionImportScreenType.EXT_ANY
                val uuid = navBackStack.arguments?.getString("uuid")?.takeIf { it != "null" }
                ExtensionImportScreen(type, uuid)
            }
            composable(Ext.View) { navBackStack ->
                val extensionId = navBackStack.arguments?.getString("id")
                ExtensionViewScreen(id = extensionId.toString())
            }
        }
    }
}