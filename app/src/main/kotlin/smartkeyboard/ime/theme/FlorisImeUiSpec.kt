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

package smartkeyboard.ime.theme

import smartkeyboard.lib.snygg.SnyggPropertySetSpecBuilder
import smartkeyboard.lib.snygg.SnyggSpec
import smartkeyboard.lib.snygg.value.SnyggCircleShapeValue
import smartkeyboard.lib.snygg.value.SnyggCutCornerDpShapeValue
import smartkeyboard.lib.snygg.value.SnyggCutCornerPercentShapeValue
import smartkeyboard.lib.snygg.value.SnyggDpSizeValue
import smartkeyboard.lib.snygg.value.SnyggRectangleShapeValue
import smartkeyboard.lib.snygg.value.SnyggRoundedCornerDpShapeValue
import smartkeyboard.lib.snygg.value.SnyggRoundedCornerPercentShapeValue
import smartkeyboard.lib.snygg.value.SnyggSolidColorValue
import smartkeyboard.lib.snygg.value.SnyggSpSizeValue

fun SnyggPropertySetSpecBuilder.background() {
    property(
        name = smartkeyboard.lib.snygg.Snygg.Background,
        level = smartkeyboard.lib.snygg.SnyggLevel.BASIC,
        supportedValues(SnyggSolidColorValue),
    )
}
fun SnyggPropertySetSpecBuilder.foreground() {
    property(
        name = smartkeyboard.lib.snygg.Snygg.Foreground,
        level = smartkeyboard.lib.snygg.SnyggLevel.BASIC,
        supportedValues(SnyggSolidColorValue),
    )
}
fun SnyggPropertySetSpecBuilder.border() {
    property(
        name = smartkeyboard.lib.snygg.Snygg.BorderColor,
        level = smartkeyboard.lib.snygg.SnyggLevel.ADVANCED,
        supportedValues(SnyggSolidColorValue),
    )
    property(
        name = smartkeyboard.lib.snygg.Snygg.BorderWidth,
        level = smartkeyboard.lib.snygg.SnyggLevel.ADVANCED,
        supportedValues(SnyggDpSizeValue),
    )
}
fun SnyggPropertySetSpecBuilder.font() {
    property(
        name = smartkeyboard.lib.snygg.Snygg.FontSize,
        level = smartkeyboard.lib.snygg.SnyggLevel.ADVANCED,
        supportedValues(SnyggSpSizeValue),
    )
}
fun SnyggPropertySetSpecBuilder.shadow() {
    property(
        name = smartkeyboard.lib.snygg.Snygg.ShadowElevation,
        level = smartkeyboard.lib.snygg.SnyggLevel.ADVANCED,
        supportedValues(SnyggDpSizeValue),
    )
}
fun SnyggPropertySetSpecBuilder.shape() {
    property(
        name = smartkeyboard.lib.snygg.Snygg.Shape,
        level = smartkeyboard.lib.snygg.SnyggLevel.ADVANCED,
        supportedValues(
            SnyggRectangleShapeValue,
            SnyggCircleShapeValue,
            SnyggRoundedCornerDpShapeValue,
            SnyggRoundedCornerPercentShapeValue,
            SnyggCutCornerDpShapeValue,
            SnyggCutCornerPercentShapeValue,
        ),
    )
}

object FlorisImeUiSpec : SnyggSpec({
    element(FlorisImeUi.Keyboard) {
        background()
    }
    element(FlorisImeUi.Key) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(FlorisImeUi.KeyHint) {
        background()
        foreground()
        font()
        shape()
    }
    element(FlorisImeUi.KeyPopup) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }

    element(FlorisImeUi.Smartbar) {
        background()
    }
    element(FlorisImeUi.SmartbarSharedActionsRow) {
        background()
    }
    element(FlorisImeUi.SmartbarSharedActionsToggle) {
        background()
        foreground()
        shape()
        shadow()
        border()
    }
    element(FlorisImeUi.SmartbarExtendedActionsRow) {
        background()
    }
    element(FlorisImeUi.SmartbarExtendedActionsToggle) {
        background()
        foreground()
        shape()
        shadow()
        border()
    }
    element(FlorisImeUi.SmartbarActionKey) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(FlorisImeUi.SmartbarActionTile) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(FlorisImeUi.SmartbarActionsOverflowCustomizeButton) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(FlorisImeUi.SmartbarActionsOverflow) {
        background()
    }
    element(FlorisImeUi.SmartbarActionsEditor) {
        background()
        shape()
    }
    element(FlorisImeUi.SmartbarActionsEditorHeader) {
        background()
        foreground()
        font()
    }
    element(FlorisImeUi.SmartbarActionsEditorSubheader) {
        foreground()
        font()
    }
    element(FlorisImeUi.SmartbarCandidatesRow) {
        background()
    }
    element(FlorisImeUi.SmartbarCandidateWord) {
        background()
        foreground()
        font()
        shape()
    }
    element(FlorisImeUi.SmartbarCandidateClip) {
        background()
        foreground()
        font()
        shape()
    }
    element(FlorisImeUi.SmartbarCandidateSpacer) {
        foreground()
    }

    element(FlorisImeUi.ClipboardHeader) {
        background()
        foreground()
        font()
    }
    element(FlorisImeUi.ClipboardItem) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(FlorisImeUi.ClipboardItemPopup) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }

    element(FlorisImeUi.EmojiKey) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(FlorisImeUi.EmojiKeyPopup) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(FlorisImeUi.EmojiTab) {
        foreground()
    }

    element(FlorisImeUi.ExtractedLandscapeInputLayout) {
        background()
    }
    element(FlorisImeUi.ExtractedLandscapeInputField) {
        background()
        foreground()
        font()
        shape()
        border()
    }
    element(FlorisImeUi.ExtractedLandscapeInputAction) {
        background()
        foreground()
        shape()
    }

    element(FlorisImeUi.GlideTrail) {
        foreground()
    }

    element(FlorisImeUi.IncognitoModeIndicator) {
        foreground()
    }

    element(FlorisImeUi.OneHandedPanel) {
        background()
        foreground()
    }

    element(FlorisImeUi.SystemNavBar) {
        background()
    }
})
