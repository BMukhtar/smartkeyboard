package trie

import kotlin.math.max
import kotlin.math.min

data class Term(val word: String, val frequency: Long)

//Trie node class
data class Node(
    //Does this node represent the last character in a word?
    //0: no word; >0: is word (termFrequencyCount)
    var frequency: Long,
    var children: MutableList<NodeChild> = mutableListOf(),
    var maxChildFrequency: Long = 0,
)

data class NodeChild(val key: String, val node: Node)

class PruningRadixTrie {
    var termCount: Long = 0

    //The trie
    private val root: Node = Node(0)

    fun addTerm(term: Term) {
        val nodeList: MutableList<Node> = ArrayList()
        addTerm(root, term.word, term.frequency, 0, 0, nodeList)
    }

    fun updateMaxCounts(nodeList: List<Node>, termFrequencyCount: Long) {
        for (node in nodeList) {
            if (termFrequencyCount > node.maxChildFrequency) {
                node.maxChildFrequency = termFrequencyCount
            }
        }
    }

    fun addTerm(curr: Node, term: String, termFrequencyCount: Long, id: Int, level: Int, nodeList: MutableList<Node>) {
        try {
            nodeList.add(curr)

            //test for common prefix (with possibly different suffix)
            var common = 0
            val currChildren = curr.children
            for (j in currChildren.indices) {
                val key = currChildren[j].key
                val node = currChildren[j].node
                for (i in 0 until min(term.length, key.length)) {
                    common = if (term[i] == key[i]) i + 1 else break
                }
                if (common > 0) {
                    //term already existed
                    //existing ab
                    //new      ab
                    if (common == term.length && common == key.length) {
                        if (node.frequency == 0L) termCount++
                        node.frequency = node.frequency + termFrequencyCount
                        updateMaxCounts(nodeList, node.frequency)
                    } else if (common == term.length) {
                        //insert second part of oldKey as child
                        val child = Node(termFrequencyCount)
                        val l: MutableList<NodeChild> = ArrayList()
                        l.add(NodeChild(key.substring(common), node))
                        child.children = l
                        child.maxChildFrequency = max(node.maxChildFrequency, node.frequency)
                        updateMaxCounts(nodeList, termFrequencyCount)

                        //insert first part as key, overwrite old node
                        currChildren[j] = NodeChild(term.substring(0, common), child)
                        //sort children descending by termFrequencyCountChildMax to start lookup with most promising branch
                        currChildren.sortByDescending { it.node.maxChildFrequency }
                        //increment termcount by 1
                        termCount++
                    } else if (common == key.length) {
                        addTerm(node, term.substring(common), termFrequencyCount, id, level + 1, nodeList)
                    } else {
                        //insert second part of oldKey and of s as child
                        val child = Node(0) //count
                        val l: MutableList<NodeChild> = ArrayList()
                        l.add(NodeChild(key.substring(common), node))
                        l.add(NodeChild(term.substring(common), Node(termFrequencyCount)))
                        child.children = l
                        child.maxChildFrequency = max(
                            node.maxChildFrequency,
                            max(termFrequencyCount, node.frequency)
                        )
                        updateMaxCounts(nodeList, termFrequencyCount)

                        //insert first part as key, overwrite old node
                        currChildren[j] = NodeChild(term.substring(0, common), child)
                        //sort children descending by termFrequencyCountChildMax to start lookup with most promising branch
                        currChildren.sortByDescending { it.node.maxChildFrequency }
                        //increment termcount by 1
                        termCount++
                    }
                    return
                }
            }

            currChildren.add(NodeChild(term, Node(termFrequencyCount)))
            //sort children descending by termFrequencyCountChildMax to start lookup with most promising branch
            currChildren.sortByDescending { it.node.maxChildFrequency }
            termCount++
            updateMaxCounts(nodeList, termFrequencyCount)
        } catch (e: Exception) {
            println("exception: " + term + " " + e.message)
        }
    }

    fun findAllChildTerms(
        prefix: String = "", // actual prefix like: wo for word
        curr: Node = root,
        topK: Int,
        prefixString: String = "",
        results: MutableList<Term> = mutableListOf(),
        visitor: Visitor? = null,
        pruning: Boolean
    ) // Removed 4th parameter: ref long termfrequencyCountPrefix
    {
        try {
            //pruning/early termination in radix trie lookup
            if (pruning && topK > 0 && results.size == topK && curr.maxChildFrequency <= results[topK - 1].frequency) {
                return
            }

            //test for common prefix (with possibly different suffix)
            for (nodeChild in curr.children) {
                val key = nodeChild.key
                val node = nodeChild.node
                //pruning/early termination in radix trie lookup
                if (pruning
                    && topK > 0
                    && results.size == topK
                    && node.frequency <= results[topK - 1].frequency
                    && node.maxChildFrequency <= results[topK - 1].frequency
                ) {
                    if (prefix.isNotEmpty()) break else continue
                }
                if (prefix.isEmpty() || key.startsWith(prefix)) {
                    if (node.frequency > 0) {
                        // if (prefix == key) termfrequencyCountPrefix = node.getTermFrequencyCount();

                        //candidate
                        visitor?.visit(prefixString, key, node.frequency)
                            ?: if (topK > 0) addTopKSuggestion(
                                word = prefixString + key,
                                frequency = node.frequency,
                                topK = topK,
                                results = results
                            ) else results.add(Term(prefixString + key, node.frequency))
                    }
                    if (node.children.isNotEmpty()) {
                        findAllChildTerms(
                            curr = node,
                            topK = topK,
                            prefixString = prefixString + key,
                            results = results,
                            visitor = visitor,
                            pruning = pruning,
                        )
                    }
                    if (prefix.isNotEmpty()) break
                } else if (prefix.startsWith(key)) {
                    if (node.children.isNotEmpty()) {
                        findAllChildTerms(
                            prefix = prefix.substring(key.length),
                            curr = node,
                            topK = topK,
                            prefixString = prefixString + key,
                            results = results,
                            visitor = visitor,
                            pruning = pruning
                        )
                    }
                    break
                }
            }
        } catch (e: Exception) {
            println("exception: " + prefix + " " + e.message)
        }
    }

    fun getTopkTermsForPrefix(
        prefix: String,
        topK: Int,
        pruning: Boolean = true,
    ): List<Term> { // Removed parameter 'out long termFrequencyCountPrefix' as returning it in Java would mean changing the return type of the method.
        val results: MutableList<Term> = ArrayList()

        //termFrequency of prefix, if it exists in the dictionary (even if not returned in the topK results due to low termFrequency)
        // long termFrequencyCountPrefix = 0;

        // At the end of the prefix, find all child words
        findAllChildTerms(
            prefix = prefix,
            topK = topK,
            results = results,
            pruning = pruning
        )
        return results
    }

    fun addTopKSuggestion(word: String, frequency: Long, topK: Int, results: MutableList<Term>) {
        //at the end/highest index is the lowest value
        // >  : old take precedence for equal rank   
        // >= : new take precedence for equal rank 
        if (results.size < topK || frequency >= results[topK - 1].frequency) {
            val term = Term(word, frequency)
            val index = results.binarySearch(term, Comparator.comparing { item: Term -> item.frequency }.reversed())

            if (index < 0) results.add(index.inv(), term) else results.add(index, term)
            if (results.size > topK) results.removeAt(topK)
        }
    }
}


fun interface Visitor {
    fun visit(prefix: String, key: String, freq: Long)
}