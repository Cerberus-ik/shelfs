package de.treona.leagueTools.util

import java.lang.StringBuilder
import java.security.SecureRandom

class RandomCodeGenerator {

    private val random = SecureRandom()
    private val alphabet = charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
            'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9')

    fun generateCode(length: Int): String {
        val stringBuilder = StringBuilder()
        for (i in 1..length) {
            stringBuilder.append(alphabet[this.random.nextInt(alphabet.size)])
        }
        return stringBuilder.toString()
    }
}