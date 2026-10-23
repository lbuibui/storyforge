package com.storyforge.nativerewrite.data

/**
 * Same rule as the web `countWords()`: one count per character after whitespace is removed.
 */
fun countStoryWords(plainText: String): Int {
    if (plainText.isEmpty()) return 0
    return plainText.replace(Regex("\\s"), "").length
}
