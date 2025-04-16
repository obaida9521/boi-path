package com.developerobaida.boipath.interfaces

interface ReaderOptionsListener {
    fun goToNextPages()
    fun goToPreviousPages()
    fun adjustTextSizes(size: Int)
    fun adjustTextAlignments(alignment: String)
    fun changeBackgroundColors(bgColor: String, textColor: String)
    fun adjustBrightnesses(brightness: Int)
    fun updateLineHeights(lineHeight: Float)
    fun updateLetterSpacings(letterSpacing: Float)
    fun updateWordSpacings(wordSpacing: Float)
    fun changeFonts(fontPath: String)
    fun scrollToPages(page: Int)
}