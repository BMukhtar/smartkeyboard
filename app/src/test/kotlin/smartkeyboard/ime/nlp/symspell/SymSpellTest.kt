package smartkeyboard.ime.nlp.symspell


import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors

internal class SymSpellTest {
    private val bigramsPath = Objects.requireNonNull(javaClass.classLoader.getResource("bigrams.txt"))
    private val bigrams = Files.lines(Paths.get(bigramsPath.toURI()))
        .map { line: String -> line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() }
        .collect(
            Collectors.toMap(
                { tokens: Array<String> -> Bigram(tokens[0], tokens[1]) },
                { tokens: Array<String> -> tokens[2].toLong() }
            )
        )
    private val wordsPath = Objects.requireNonNull(javaClass.classLoader.getResource("words.txt"))
    private val unigrams = Files.lines(Paths.get(wordsPath.toURI()))
        .map { line: String -> line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() }
        .collect(
            Collectors.toMap(
                { tokens: Array<String> -> tokens[0] },
                { tokens: Array<String> -> tokens[1].toLong() }
            )
        )

    @Test
    @Throws(Exception::class)
    fun loadDictionary() {
        val symSpell = SymSpellImpl(unigramLexicon = mapOf("abcde", 100L, "abcdef", 90L))
        val deletes: Map<String, Collection<String>> = symSpell.getDeletes()
        val suggestions = deletes["abcd"]!!
        Assertions.assertTrue(
            suggestions.containsAll(Arrays.asList("abcde", "abcdef")),
            "abcd == abcde - {e} (distance 1), abcd == abcdef - {ef} (distance 2)"
        )
    }

    @Test
    @Throws(Exception::class)
    fun lookupCompound() {
        val symSpell = createSymSpell(
            unigramLexicon = unigrams,
            bigramLexicon = bigrams,
            prefixLength = 10,
        )
        val suggestions = symSpell.lookupCompound(
            "whereis th elove hehad dated forImuch of thepast who couqdn'tread in sixthgrade and ins pired him".lowercase(
                Locale.getDefault()
            ), 2, false
        )
        Assertions.assertEquals(1, suggestions.size)
        Assertions.assertEquals(
            "where is the love he had dated for much of the past who couldn't read in sixth grade and inspired him",
            suggestions[0].suggestion
        )
    }

    @Test
    @Throws(Exception::class)
    fun lookupCompound2() {
        val symSpell = createSymSpell(
            unigramLexicon = unigrams,
            bigramLexicon = bigrams,
            prefixLength = 10,
        )
        val suggestions = symSpell.lookupCompound(
            "Can yu readthis messa ge despite thehorible sppelingmsitakes".lowercase(Locale.getDefault()),
            2,
            false
        )
        Assertions.assertEquals(1, suggestions.size)
        Assertions.assertEquals(
            "can you read this message despite the horrible spelling mistakes",
            suggestions[0].suggestion
        )
    }

    @Test
    @Throws(Exception::class)
    fun lookupCompoundWithUnknownWords() {
        val symSpell = createSymSpell(
            unigramLexicon = unigrams,
            bigramLexicon = bigrams,
        )
        val suggestions = symSpell.lookupCompound(
            "Atrociraptor wasassigned to the Velociraptorinae within a larger Dromaeosauridae",
            1,
            false
        )
        Assertions.assertEquals(
            "Atrociraptor was assigned to the Velociraptorinae within a larger Dromaeosauridae",
            suggestions[0].suggestion
        )
    }

    @Test
    @Throws(Exception::class)
    fun lookupWordWithNoErrors() {
        val symSpell = createSymSpell(
            unigramLexicon = unigrams,
            maxDictionaryEditDistance = 3,
        )
        val suggestions = symSpell.lookup("questionnaire", Verbosity.CLOSEST)
        Assertions.assertEquals(1, suggestions.size)
        Assertions.assertEquals("questionnaire", suggestions[0].suggestion)
        Assertions.assertEquals(0, suggestions[0].editDistance)
    }

    @Test
    @Throws(Exception::class)
    fun noSuggestionFound() {
        val symSpell = createSymSpell(unigramLexicon = unigrams)
        val suggestions = symSpell.lookup("qwertyuiop", Verbosity.ALL, false)
        Assertions.assertTrue(suggestions.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun noSuggestionFoundIncludeUnknown() {
        val symSpell = createSymSpell(unigramLexicon = unigrams)
        val input = "qwertyuiop"
        val suggestions = symSpell.lookup(input, Verbosity.ALL, true)
        Assertions.assertFalse(suggestions.isEmpty())
        Assertions.assertEquals(input, suggestions[0].suggestion)
    }

    @Test
    @Throws(Exception::class)
    fun combineWords() {
        val symSpell = createSymSpell(
            unigramLexicon = unigrams,
            bigramLexicon = bigrams,
            prefixLength = 10,
        )
        val newSuggestion = symSpell.combineWords(
            2,
            false,
            "pired",
            "ins",
            SuggestItem("in", 1, 8.46E9),
            SuggestItem("tired", 1, 1.1E7)
        )
        Assertions.assertTrue(newSuggestion.isPresent)
        Assertions.assertEquals(
            SuggestItem("inspired", 0, symSpell.unigramLexicon["inspired"]!!.toDouble()),
            newSuggestion.get()
        )
    }

    @Test
    @Throws(Exception::class)
    fun lookupWithoutLoadingDictThrowsException() {
        val symSpell = createSymSpell()
        Assertions.assertThrows(NotInitializedException::class.java) { symSpell.lookup("boom", Verbosity.CLOSEST) }
    }

    @Test
    @Throws(Exception::class)
    fun lookupAll() {
        val symSpell = createSymSpell(unigramLexicon = unigrams)
        val suggestions = symSpell.lookup("sumarized", Verbosity.ALL)
        Assertions.assertEquals(6, suggestions.size)
        Assertions.assertEquals("summarized", suggestions[0].suggestion)
        Assertions.assertEquals(1, suggestions[0].editDistance)
    }

    @Test
    @Throws(Exception::class)
    fun editsDistance0() {
        val symSpell = createSymSpell(maxDictionaryEditDistance = 0)
        val edits = symSpell.edits("example", 0, HashSet())
        Assertions.assertEquals(emptySet<Any>(), edits)
    }

    @Test
    @Throws(Exception::class)
    fun editsDistance1() {
        val symSpell = createSymSpell(maxDictionaryEditDistance = 1)
        val edits = symSpell.edits("example", 0, HashSet())
        Assertions.assertEquals(setOf("xample", "eample", "exmple", "exaple", "examle", "exampe", "exampl"), edits)
    }

    @Test
    @Throws(Exception::class)
    fun editsDistance2() {
        val symSpell = createSymSpell(maxDictionaryEditDistance = 2)
        val edits = symSpell.edits("example", 0, HashSet())
        val expected = setOf(
            "xample", "eample", "exmple", "exaple", "examle", "exampe", "exampl", "exale", "emple",
            "exape", "exmpe", "exapl", "xampe", "exple", "exmpl", "exmle", "xamle", "xmple",
            "exame", "xaple", "xampl", "examl", "eaple", "eampl", "examp", "ample", "eamle",
            "eampe"
        )
        Assertions.assertEquals(expected, edits)
    }

    @Test
    @Throws(NotInitializedException::class)
    fun customStringDistanceAlgorithm() {
        val hammingDistance = object : StringDistance {
            override fun distanceWithEarlyStop(string1: String, string2: String, maxDistance: Int): Int {
                if (string1.length != string2.length) {
                    return -1
                }
                val chars1 = string1.toCharArray()
                val chars2 = string2.toCharArray()
                var distance = 0
                for (i in chars1.indices) {
                    val c1 = chars1[i]
                    val c2 = chars2[i]
                    if (c1 != c2) {
                        distance += 1
                    }
                }
                return distance
            }
        }
        val symSpell = createSymSpell(
            unigramLexicon = mapOf("1001001", 1L),
            bigramLexicon = bigrams,
            maxDictionaryEditDistance = 1,
            stringDistance = hammingDistance,
        )
        val suggestions = symSpell.lookup("1000001", Verbosity.CLOSEST)
        Assertions.assertEquals(1, suggestions[0].editDistance)
    }

    private fun createSymSpell(
        unigramLexicon: Map<String, Long> = emptyMap(),
        bigramLexicon: Map<Bigram, Long> = emptyMap(),
        maxDictionaryEditDistance: Int = 2,
        prefixLength: Int = 7,
        stringDistance: StringDistance = DamerauLevenshteinOSA(),
    ): SymSpellImpl {
        return SymSpellImpl(
            unigramLexicon,
            bigramLexicon,
            maxDictionaryEditDistance,
            prefixLength,
            stringDistance,
        )
    }

    companion object {
        fun <T> mapOf(vararg objects: Any): Map<String, T> {
            val map: MutableMap<String, T> = HashMap()
            var i = 0
            while (i < objects.size) {
                map[objects[i] as String] = objects[i + 1] as T
                i += 2
            }
            return map
        }

        fun <T> setOf(vararg values: T): Set<T> {
            return Arrays.stream(values).collect(Collectors.toSet())
        }
    }
}
