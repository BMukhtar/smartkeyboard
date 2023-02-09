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

package smartkeyboard.app.settings.localization

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import smartkeyboard.R
import smartkeyboard.app.LocalNavController
import smartkeyboard.app.Routes
import smartkeyboard.app.florisPreferenceModel
import smartkeyboard.ime.core.DisplayLanguageNamesIn
import smartkeyboard.ime.core.Subtype
import smartkeyboard.ime.core.SubtypeJsonConfig
import smartkeyboard.ime.core.SubtypeLayoutMap
import smartkeyboard.ime.core.SubtypeNlpProviderMap
import smartkeyboard.ime.core.SubtypePreset
import smartkeyboard.ime.keyboard.LayoutArrangementComponent
import smartkeyboard.ime.keyboard.LayoutType
import smartkeyboard.ime.keyboard.extCorePopupMapping
import smartkeyboard.keyboardManager
import smartkeyboard.lib.FlorisLocale
import smartkeyboard.lib.compose.FlorisButtonBar
import smartkeyboard.lib.compose.FlorisDropdownLikeButton
import smartkeyboard.lib.compose.FlorisDropdownMenu
import smartkeyboard.lib.compose.FlorisScreen
import smartkeyboard.lib.compose.stringRes
import smartkeyboard.lib.ext.ExtensionComponentName
import smartkeyboard.lib.observeAsNonNullState
import smartkeyboard.subtypeManager
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import dev.patrickgold.jetpref.material.ui.JetPrefListItem
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import smartkeyboard.ime.keyboard.DefaultComputingEvaluator.subtype
import smartkeyboard.ime.text.composing.Appender.id

private val SelectComponentName = ExtensionComponentName("00", "00")
private val SelectNlpProviderId = SelectComponentName.toString()
private val SelectNlpProviders = SubtypeNlpProviderMap(
    spelling = SelectNlpProviderId,
)
private val SelectLayoutMap = SubtypeLayoutMap(
    characters = SelectComponentName,
    symbols = SelectComponentName,
    symbols2 = SelectComponentName,
    numeric = SelectComponentName,
    numericAdvanced = SelectComponentName,
    numericRow = SelectComponentName,
    phone = SelectComponentName,
    phone2 = SelectComponentName,
)
private val SelectLocale = FlorisLocale.from("00", "00")
private val SelectListKeys = listOf(SelectComponentName)

private class SubtypeAddState(init: Subtype?) {
    companion object {
        val Saver = Saver<SubtypeAddState, String>(
            save = { editor ->
                val subtype = Subtype(
                    id = editor.id.value,
                    primaryLocale = editor.primaryLocale.value,
                    secondaryLocales = editor.secondaryLocales.value,
                    nlpProviders = editor.nlpProviders.value,
                    composer = editor.composer.value,
                    currencySet = editor.currencySet.value,
                    punctuationRule = editor.punctuationRule.value,
                    popupMapping = editor.popupMapping.value,
                    layoutMap = editor.layoutMap.value,
                )
                SubtypeJsonConfig.encodeToString(subtype)
            },
            restore = { str ->
                val subtype = SubtypeJsonConfig.decodeFromString<Subtype>(str)
                SubtypeAddState(subtype)
            },
        )
    }

    val id: MutableState<Long> = mutableStateOf(init?.id ?: -1)
    val primaryLocale: MutableState<FlorisLocale> = mutableStateOf(init?.primaryLocale ?: SelectLocale)
    val secondaryLocales: MutableState<List<FlorisLocale>> = mutableStateOf(init?.secondaryLocales ?: listOf())
    val nlpProviders: MutableState<SubtypeNlpProviderMap> =
        mutableStateOf(init?.nlpProviders ?: Subtype.DEFAULT.nlpProviders)
    val composer: MutableState<ExtensionComponentName> = mutableStateOf(init?.composer ?: SelectComponentName)
    val currencySet: MutableState<ExtensionComponentName> = mutableStateOf(init?.currencySet ?: SelectComponentName)
    val punctuationRule: MutableState<ExtensionComponentName> =
        mutableStateOf(init?.punctuationRule ?: Subtype.DEFAULT.punctuationRule)
    val popupMapping: MutableState<ExtensionComponentName> = mutableStateOf(init?.popupMapping ?: SelectComponentName)
    val layoutMap: MutableState<SubtypeLayoutMap> = mutableStateOf(init?.layoutMap ?: SelectLayoutMap)

    fun applySubtype(subtype: Subtype) {
        id.value = subtype.id
        primaryLocale.value = subtype.primaryLocale
        secondaryLocales.value = subtype.secondaryLocales
        composer.value = subtype.composer
        currencySet.value = subtype.currencySet
        punctuationRule.value = subtype.punctuationRule
        popupMapping.value = subtype.popupMapping
        layoutMap.value = subtype.layoutMap
        nlpProviders.value = subtype.nlpProviders
    }

    fun toSubtype() = runCatching<Subtype> {
        check(primaryLocale.value != SelectLocale)
        check(nlpProviders.value.spelling != SelectNlpProviderId)
        check(nlpProviders.value.suggestion != SelectNlpProviderId)
        check(composer.value != SelectComponentName)
        check(currencySet.value != SelectComponentName)
        check(punctuationRule.value != SelectComponentName)
        check(popupMapping.value != SelectComponentName)
        check(layoutMap.value.characters != SelectComponentName)
        check(layoutMap.value.symbols != SelectComponentName)
        check(layoutMap.value.symbols2 != SelectComponentName)
        check(layoutMap.value.numeric != SelectComponentName)
        check(layoutMap.value.numericAdvanced != SelectComponentName)
        check(layoutMap.value.numericRow != SelectComponentName)
        check(layoutMap.value.phone != SelectComponentName)
        check(layoutMap.value.phone2 != SelectComponentName)
        Subtype(
            id.value, primaryLocale.value, secondaryLocales.value, nlpProviders.value, composer.value,
            currencySet.value, punctuationRule.value, popupMapping.value, layoutMap.value,
        )
    }
}

@Composable
fun SubtypeSimpleAddScreen() = FlorisScreen {
    title = stringRes(R.string.settings__localization__subtype_add_title)

    val selectValue = stringRes(R.string.settings__localization__subtype_select_placeholder)
    val selectListValues = remember(selectValue) { listOf(selectValue) }

    val prefs by florisPreferenceModel()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val keyboardManager by context.keyboardManager()
    val subtypeManager by context.subtypeManager()

    val displayLanguageNamesIn by prefs.localization.displayLanguageNamesIn.observeAsState()
    val composers by keyboardManager.resources.composers.observeAsNonNullState()
    val currencySets by keyboardManager.resources.currencySets.observeAsNonNullState()
    val layoutExtensions by keyboardManager.resources.layouts.observeAsNonNullState()
    val popupMappings by keyboardManager.resources.popupMappings.observeAsNonNullState()
    val subtypePresets by keyboardManager.resources.subtypePresets.observeAsNonNullState()
    val id = null

    val subtypeEditor = SubtypeAddState(null)
    var primaryLocale by subtypeEditor.primaryLocale
    //var secondaryLocales by subtypeEditor.secondaryLocales
    var composer by subtypeEditor.composer
    var currencySet by subtypeEditor.currencySet
    var popupMapping by subtypeEditor.popupMapping
    var layoutMap by subtypeEditor.layoutMap

    var showSubtypePresetsDialog by rememberSaveable { mutableStateOf(false) }
    var showSelectAsError by rememberSaveable { mutableStateOf(false) }
    var errorDialogStrId by rememberSaveable { mutableStateOf<Int?>(null) }

    val selectLocaleScreenResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>(SelectLocaleScreenResultLanguageTag)
    DisposableEffect(selectLocaleScreenResult, lifecycleOwner) {
        val observer = Observer<String> { languageTag ->
            val locale = FlorisLocale.fromTag(languageTag)
            primaryLocale = locale
            val preset = subtypeManager.getSubtypePresetForLocale(locale)
            popupMapping = preset?.popupMapping ?: extCorePopupMapping("default")
        }
        selectLocaleScreenResult?.observe(lifecycleOwner, observer)
        onDispose { selectLocaleScreenResult?.removeObserver(observer) }
    }

    floatingActionButton {
        ExtendedFloatingActionButton(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringRes(R.string.settings__localization__subtype_add_title),
                )
            },
            text = {
                Text(
                    text = stringRes(R.string.settings__localization__subtype_advanced_add_title),
                )
            },
            onClick = {
                navController.popBackStack()
                navController.navigate(Routes.Settings.SubtypeAdd)
            },
        )
    }

    bottomBar {
        FlorisButtonBar {
            ButtonBarSpacer()
            ButtonBarTextButton(text = stringRes(R.string.action__cancel)) {
                navController.popBackStack()
            }
        }
    }

    content {
        val suggestedPresets = remember(subtypePresets) {
            val presets = subtypePresets.filter { it.locale.localeTag() in arrayOf("kk", "en", "ru", "tr") }
            presets
        }
        if (suggestedPresets.isNotEmpty()) {
            Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                suggestedPresets.forEach { subtypePreset ->
                    JetPrefListItem(
                        modifier = Modifier.clickable {
                            subtypeEditor.applySubtype(subtypePreset.toSubtype())
                            subtypeEditor.toSubtype().onSuccess { subtype ->
                                if (!subtypeManager.addSubtype(subtype)) {
                                    errorDialogStrId = R.string.settings__localization__subtype_error_already_exists
                                    return@clickable
                                }
                                navController.popBackStack()
                            }.onFailure {
                                showSelectAsError = true
                                errorDialogStrId = R.string.settings__localization__subtype_error_fields_no_value
                            }
                        },
                        text = when (displayLanguageNamesIn) {
                            DisplayLanguageNamesIn.SYSTEM_LOCALE -> subtypePreset.locale.displayName()
                            DisplayLanguageNamesIn.NATIVE_LOCALE -> subtypePreset.locale.displayName(subtypePreset.locale)
                        },
                        secondaryText = subtypePreset.preferred.characters.componentId,
                    )
                }
            }
        } else {
            Text(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                text = stringRes(R.string.settings__localization__suggested_subtype_presets_none_found),
            )
        }

        errorDialogStrId?.let { strId ->
            JetPrefAlertDialog(
                title = stringRes(R.string.error__title),
                confirmLabel = stringRes(android.R.string.ok),
                onConfirm = {
                    errorDialogStrId = null
                },
                onDismiss = {
                    errorDialogStrId = null
                },
            ) {
                Text(text = stringRes(strId))
            }
        }
    }
}
