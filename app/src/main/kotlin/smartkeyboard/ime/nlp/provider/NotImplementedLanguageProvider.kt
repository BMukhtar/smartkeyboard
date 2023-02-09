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

package smartkeyboard.ime.nlp.provider

import smartkeyboard.ime.core.Subtype
import smartkeyboard.ime.editor.EditorContent
import smartkeyboard.ime.nlp.SpellingProvider
import smartkeyboard.ime.nlp.SpellingResult
import smartkeyboard.ime.nlp.SuggestionCandidate
import smartkeyboard.ime.nlp.SuggestionProvider

object NotImplementedLanguageProvider : SpellingProvider, SuggestionProvider {

    override val providerId: String = "org.florisboard.nlp.providers.none"

    override suspend fun create() = Unit

    override suspend fun preload(subtype: Subtype) = Unit

    override suspend fun destroy() = Unit

    override suspend fun spell(
        subtype: Subtype,
        word: String,
        precedingWords: List<String>,
        followingWords: List<String>,
        maxSuggestionCount: Int,
        allowPossiblyOffensive: Boolean,
        isPrivateSession: Boolean
    ): SpellingResult = SpellingResult.unspecified()

    override suspend fun suggest(
        subtype: Subtype,
        content: EditorContent,
        maxCandidateCount: Int,
        allowPossiblyOffensive: Boolean,
        isPrivateSession: Boolean
    ): List<SuggestionCandidate> = emptyList()

    override suspend fun notifySuggestionAccepted(subtype: Subtype, candidate: SuggestionCandidate) {
        Unit
    }

    override suspend fun notifySuggestionReverted(subtype: Subtype, candidate: SuggestionCandidate) {
        Unit
    }

    override suspend fun removeSuggestion(subtype: Subtype, candidate: SuggestionCandidate): Boolean = false

    override suspend fun getListOfWords(subtype: Subtype): List<String> = emptyList()

    override suspend fun getFrequencyForWord(subtype: Subtype, word: String): Double = 0.0
}
