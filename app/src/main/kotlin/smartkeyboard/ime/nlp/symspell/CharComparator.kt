package smartkeyboard.ime.nlp.symspell

/**
 * Extends the strategy for comparing characters
 */
interface CharComparator {
    fun areEqual(ch1: Char, ch2: Char): Boolean {
        return ch1 == ch2
    }

    fun areDistinct(ch1: Char, ch2: Char): Boolean {
        return !areEqual(ch1, ch2)
    }
}

class DefaultCharComparator : CharComparator
