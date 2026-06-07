package ro.tuiasi.pp.lab02

import org.graalvm.polyglot.Context

/**
 * Cod JavaScript care calculează suma de control (checksum) a unui cuvânt.
 * Suma de control = suma valorilor ASCII ale caracterelor.
 *
 * Această constantă este folosită de [computeChecksumJS].
 * NU modifica această funcție JavaScript.
 */
val CHECKSUM_JS = """
    function checksum(word) {
        var sum = 0;
        for (var i = 0; i < word.length; i++) {
            sum += word.charCodeAt(i);
        }
        return sum;
    }
""".trimIndent()

/**
 * Calculează suma de control a unui cuvânt folosind motorul JavaScript din GraalVM.
 *
 * Pre-condiții: [word] nu este null; motorul GraalVM este disponibil.
 * Post-condiții: returnează un Int egal cu suma valorilor ASCII ale caracterelor din [word].
 *
 * Exemplu:
 * computeChecksumJS("abc") == 97 + 98 + 99 == 294
 */
fun computeChecksumJS(word: String): Int {
    return Context.create("js").use { context ->
        // Evaluam string-ul cu codul JS pentru ca functia sa devina disponibila in context
        context.eval("js", CHECKSUM_JS)

        // Preluam functia 'checksum' din contextul JS
        val jsFunction = context.getBindings("js").getMember("checksum")

        // Executam functia transmitand parametrul 'word' si returnam rezultatul ca Int
        jsFunction.execute(word).asInt()
    }
}

/**
 * Convertește lista de cuvinte la MAJUSCULE folosind motorul JavaScript din GraalVM.
 *
 * Pre-condiții: [words] poate fi goală.
 * Post-condiții: returnează o nouă listă cu fiecare cuvânt transformat prin .toUpperCase() JS.
 *
 * Exemplu:
 * upperCaseWordsJS(listOf("ana", "mere")) == listOf("ANA", "MERE")
 */
fun upperCaseWordsJS(words: List<String>): List<String> {
    return Context.create("js").use { context ->
        words.map { word ->
            // Injectam cuvantul curent in contextul JS intr-o variabila numita "currentWord"
            context.getBindings("js").putMember("currentWord", word)

            // Evaluam expresia care apeleaza .toUpperCase() pe variabila noastra
            context.eval("js", "currentWord.toUpperCase()").asString()
        }
    }
}

/**
 * Grupează cuvintele după suma lor de control și returnează grupurile cu cel puțin 2 cuvinte.
 *
 * Pre-condiții: [words] poate fi goală.
 * Post-condiții:
 * - returnează un Map<Int, List<String>> unde cheia este suma de control
 * - în map apar doar grupurile cu >= 2 cuvinte (cuvinte cu aceeași sumă de control)
 * - cuvintele din fiecare grup sunt în ordinea în care apar în [words]
 *
 * Exemplu:
 * "abc" și "bca" au ambele checksum 294 → apar în același grup
 * "aa" are checksum 194 și e singurul → nu apare în rezultat
 */
fun groupByChecksum(words: List<String>): Map<Int, List<String>> {
    return words
        // Grupam cuvintele folosind checksum-ul ca si cheie
        .groupBy { computeChecksumJS(it) }
        // Pastram doar grupurile care au o lista (valoare) de dimensiune >= 2
        .filter { it.value.size >= 2 }
}

/**
 * Afișează la consolă grupurile de cuvinte cu aceeași sumă de control.
 * Format: "Checksum <N>: [cuvant1, cuvant2, ...]"
 *
 * Exemplu pentru groupByChecksum(listOf("abc","bca","hello","aa")):
 * Checksum 294: [abc, bca]
 */
fun printDuplicateChecksums(words: List<String>) {
    val groupedWords = groupByChecksum(words)

    // Iteram prin dictionar si afisam conform formatului cerut
    for ((checksum, wordList) in groupedWords) {
        println("Checksum $checksum: $wordList")
    }
}