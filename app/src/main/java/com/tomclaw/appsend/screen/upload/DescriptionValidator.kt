package com.tomclaw.appsend.screen.upload

interface DescriptionValidator {

    fun calculateEffectiveLength(text: String): Int

    fun isValid(text: String): Boolean

}

class DescriptionValidatorImpl : DescriptionValidator {

    override fun calculateEffectiveLength(text: String): Int {
        if (text.isEmpty()) return 0

        var length = 0
        var prevWasSpaceOrPunctuation = false

        for (char in text) {
            val isSpaceOrPunctuation = char.isWhitespace() || char.isPunctuation()

            if (isSpaceOrPunctuation) {
                if (!prevWasSpaceOrPunctuation) {
                    length++
                }
                prevWasSpaceOrPunctuation = true
            } else {
                length++
                prevWasSpaceOrPunctuation = false
            }
        }

        return length
    }

    override fun isValid(text: String): Boolean {
        return calculateEffectiveLength(text) >= MIN_DESCRIPTION_LENGTH
    }

    private fun Char.isPunctuation(): Boolean {
        return this in PUNCTUATION_CHARS || category in PUNCTUATION_CATEGORIES
    }

    companion object {
        const val MIN_DESCRIPTION_LENGTH = 100

        private val PUNCTUATION_CHARS = setOf(
            '.', ',', '!', '?', ':', ';', '-', '–', '—',
            '(', ')', '[', ']', '{', '}',
            '"', '\'', '«', '»', '"', '"', 
            '/', '\\', '|', '@', '#', '$', '%', '^', '&', '*',
            '+', '=', '<', '>', '~', '`'
        )

        private val PUNCTUATION_CATEGORIES = setOf(
            CharCategory.DASH_PUNCTUATION,
            CharCategory.START_PUNCTUATION,
            CharCategory.END_PUNCTUATION,
            CharCategory.CONNECTOR_PUNCTUATION,
            CharCategory.OTHER_PUNCTUATION,
            CharCategory.INITIAL_QUOTE_PUNCTUATION,
            CharCategory.FINAL_QUOTE_PUNCTUATION
        )
    }

}

