package smartkeyboard.ime.nlp.symspell

import java.util.*

data class SuggestItem(val suggestion: String, val editDistance: Int, val frequencyOfSuggestionInDict: Double) :
    Comparable<SuggestItem> {

    /**
     * Compares this `SuggestItem` with the specified `SuggestItem`.
     * It will first sort by [SuggestItem.getEditDistance], and then by [SuggestItem.getFrequencyOfSuggestionInDict]
     *
     * @param suggestItem `SuggestItem` to which this `SuggestItem` is to be compared.
     * @return 0 if this `SuggestItem`'s edit distance, and frequency of suggestion are the same as `suggestItem`'s
     * 1 if this `SuggestItem`'s edit distance is greater than `suggestItem`'s, or if they are equal, this `SuggestItem`'s frequency of suggestion is lower
     * -1 if this `SuggestItem`'s edit distance is lower than `suggestItem`'s, or if it's equal and the frequency is greater
     */
    override fun compareTo(suggestItem: SuggestItem): Int {
        return if (editDistance == suggestItem.editDistance) {
            // Descending
            suggestItem.frequencyOfSuggestionInDict.compareTo(frequencyOfSuggestionInDict)
        } else {
            // Ascending
            editDistance.compareTo(suggestItem.editDistance)
        }
    }
}

/**
 * Holds a pair of words.
 */
data class Bigram(private val word1: String, private val word2: String) {
    override fun toString(): String {
        return "$word1 $word2"
    }
}

/**
 * Allows to control the number of returned results
 */
enum class Verbosity {
    /**
     * Top suggestion with the highest term frequency of the suggestions of smallest edit distance found
     */
    TOP,

    /**
     * All suggestions of smallest edit distance found
     */
    CLOSEST,

    /**
     * All suggestions within [SymSpell.getMaxDictionaryEditDistance]
     */
    ALL
}

open class JSymSpellException : Exception {
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
}

class NotInitializedException(message: String?) : JSymSpellException(message)
