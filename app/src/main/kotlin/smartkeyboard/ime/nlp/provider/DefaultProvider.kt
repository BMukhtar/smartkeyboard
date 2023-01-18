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

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import smartkeyboard.appContext
import smartkeyboard.ime.core.Subtype
import smartkeyboard.ime.editor.EditorContent
import smartkeyboard.ime.nlp.SpellingProvider
import smartkeyboard.ime.nlp.SpellingResult
import smartkeyboard.ime.nlp.SuggestionCandidate
import smartkeyboard.ime.nlp.SuggestionProvider
import smartkeyboard.ime.nlp.WordSuggestionCandidate
import smartkeyboard.ime.nlp.symspell.Bigram
import smartkeyboard.ime.nlp.symspell.CharComparator
import smartkeyboard.ime.nlp.symspell.DamerauLevenshteinOSA
import smartkeyboard.ime.nlp.symspell.SuggestItem
import smartkeyboard.ime.nlp.symspell.SymSpell
import smartkeyboard.ime.nlp.symspell.SymSpellImpl
import smartkeyboard.ime.nlp.symspell.Verbosity
import smartkeyboard.lib.android.reader
import smartkeyboard.lib.devtools.flogDebug
import smartkeyboard.lib.devtools.flogInfo
import trie.PruningRadixTrie
import trie.Term
import java.util.Collections.frequency

val RussianToKazakhChars = mapOf(
    'е' to 'ё',
    'а' to 'ә',
    'и' to 'і',
    'ы' to 'і',
    'н' to 'ң',
    'г' to 'ғ',
    'у' to 'ү',
    'у' to 'ұ',
    'к' to 'қ',
    'о' to 'ө',
    'х' to 'һ',
)

object KazakhCharComparator : CharComparator {

    override fun areEqual(ch1: Char, ch2: Char): Boolean {
        return ch1 == ch2 || ch1 == RussianToKazakhChars[ch2] || ch2 == RussianToKazakhChars[ch1]
    }
}

open class DefaultProvider(
    context: Context,
    override val providerId: String,
    val unigramsFileName: String,
    val bigramsFileName: String,
) : SpellingProvider, SuggestionProvider {
    companion object {
        const val MAX_DISTANCE = 1
        const val PREFIX_LENGTH = 5
    }

    private val appContext by context.appContext()

    private var symSpell: SymSpell? = null
    private var trie: PruningRadixTrie? = null
    private var maxFreq: Long = 1L

    override suspend fun create() {
        // Here we initialize our provider, set up all things which are not language dependent.
    }

    override suspend fun preload(subtype: Subtype) = withContext(Dispatchers.IO) {
        // Here we have the chance to preload dictionaries and prepare a neural network for a specific language.
        // Is kept in sync with the active keyboard subtype of the user, however a new preload does not necessary mean
        // the previous language is not needed anymore (e.g. if the user constantly switches between two subtypes)

        // To read a file from the APK assets the following methods can be used:
        // appContext.assets.open()
        // appContext.assets.reader()
        // appContext.assets.bufferedReader()
        // appContext.assets.readText()
        // To copy an APK file/dir to the file system cache (appContext.cacheDir), the following methods are available:
        // appContext.assets.copy()
        // appContext.assets.copyRecursively()

        // if already preloaded return
        if (trie != null || symSpell != null) return@withContext

        flogInfo { "Starting preloading: $providerId" }

        val unigrams = mutableMapOf<String, Long>()
        appContext.assets.reader("symspell/$unigramsFileName")
            .forEachLine { line: String ->
                val (word, countAsString) = line.split(",")
                unigrams[word] = countAsString.toLong()
            }
        maxFreq = unigrams.values.sum()

        val bigrams = mutableMapOf<Bigram, Long>()
        appContext.assets.reader("symspell/$bigramsFileName")
            .forEachLine { line: String ->
                val (word1, word2, countAsString) = line.split(" ")
                bigrams[Bigram(word1, word2)] = countAsString.toLong()
            }

        launch {
            val localTrie = PruningRadixTrie()
            unigrams.forEach { (word, frequency) -> localTrie.addTerm(Term(word, frequency)) }
            bigrams.forEach { (bigram, frequency) ->
                localTrie.addTerm(Term("${bigram.word1} ${bigram.word2}", frequency))
            }
            trie = localTrie
        }

        launch {
            symSpell = SymSpellImpl(
                unigramLexicon = unigrams,
                bigramLexicon = bigrams,
                maxDictionaryEditDistance = MAX_DISTANCE,
                prefixLength = PREFIX_LENGTH,
                stringDistance = DamerauLevenshteinOSA(KazakhCharComparator)
            )
        }
        Unit
    }

    override suspend fun spell(
        subtype: Subtype,
        word: String,
        precedingWords: List<String>,
        followingWords: List<String>,
        maxSuggestionCount: Int,
        allowPossiblyOffensive: Boolean,
        isPrivateSession: Boolean,
    ): SpellingResult {
        val suggestItems = getSpellingSuggestedItems(initialWord = word.lowercase()).ifEmpty { null }
            ?: return SpellingResult.unspecified()
        if (suggestItems.isEmpty()) {
            return SpellingResult.validWord()
        }

        return SpellingResult.typo(suggestItems.take(maxSuggestionCount).map { it.suggestion }.toTypedArray())
    }

    override suspend fun suggest(
        subtype: Subtype,
        content: EditorContent,
        maxCandidateCount: Int,
        allowPossiblyOffensive: Boolean,
        isPrivateSession: Boolean,
    ): List<SuggestionCandidate> {
        val localTrie = trie ?: return emptyList()
        val lastWord = content.currentWordText.ifEmpty {
            // when ...word1_word2_ take only word2_
            content.text.trim().split(" ").lastOrNull()?.plus(" ")
        }.orEmpty().lowercase()
        val autocompleteItems = localTrie.getTopkTermsForPrefix(prefix = lastWord, topK = maxCandidateCount)

        if (autocompleteItems.isEmpty()) {
            val spellingSuggestItems = getSpellingSuggestedItems(initialWord = content.currentWordText.lowercase())
            return spellingSuggestItems.take(maxCandidateCount).map {
                WordSuggestionCandidate(
                    text = it.suggestion,
                    secondaryText = null,
                    confidence = (MAX_DISTANCE - it.editDistance.coerceAtMost(MAX_DISTANCE - 1))
                        / MAX_DISTANCE.toDouble(),
                    isEligibleForAutoCommit = false,//n == 0 && word.startsWith("auto"),
                    // We set ourselves as the source provider so we can get notify events for our candidate
                    sourceProvider = this@DefaultProvider,
                )
            }
        }

        val maxFreq = autocompleteItems.maxBy { it.frequency }.frequency
        return autocompleteItems.map {
            WordSuggestionCandidate(
                text = if (content.currentWordText.isEmpty()) {
                    it.word.trimEnd()
                        .split(" ")
                        .lastOrNull() // take last if
                        .orEmpty()
                } else {
                    it.word
                }, // instead word1_word2 take only word2
                secondaryText = null,
                confidence = it.frequency / maxFreq.toDouble(),
                isEligibleForAutoCommit = false,//n == 0 && word.startsWith("auto"),
                // We set ourselves as the source provider so we can get notify events for our candidate
                sourceProvider = this@DefaultProvider,
            )
        }
    }

    private fun getSpellingSuggestedItems(initialWord: String): List<SuggestItem> {
        if (initialWord.isBlank()) return emptyList()

        var index = 0
        val variations = mutableListOf(initialWord)

        while (index < initialWord.length) {
            val localList = variations.toMutableList()
            for (variation in localList) {
                val kazakhChar = RussianToKazakhChars[variation[index]] ?: continue
                val chars = variation.toCharArray()
                chars[index] = kazakhChar
                variations.add(String(chars))
            }
            index++
        }
        return symSpell?.lookupSeveral(inputs = variations, verbosity = Verbosity.CLOSEST) ?: return emptyList()
    }

    override suspend fun notifySuggestionAccepted(subtype: Subtype, candidate: SuggestionCandidate) {
        // We can use flogDebug, flogInfo, flogWarning and flogError for debug logging, which is a wrapper for Logcat
        flogDebug { candidate.toString() }
    }

    override suspend fun notifySuggestionReverted(subtype: Subtype, candidate: SuggestionCandidate) {
        flogDebug { candidate.toString() }
    }

    override suspend fun removeSuggestion(subtype: Subtype, candidate: SuggestionCandidate): Boolean {
        flogDebug { candidate.toString() }
        return false
    }

    override suspend fun getListOfWords(subtype: Subtype): List<String> {
        return symSpell?.unigramLexicon?.keys?.toList() ?: emptyList()
    }

    override suspend fun getFrequencyForWord(subtype: Subtype, word: String): Double {
        return symSpell?.unigramLexicon?.get(word)?.div(maxFreq.toDouble()) ?: 0.0
    }

    override suspend fun destroy() {
        // Here we have the chance to de-allocate memory and finish our work. However this might never be called if
        // the app process is killed (which will most likely always be the case).
        symSpell = null
        trie = null
    }
}
