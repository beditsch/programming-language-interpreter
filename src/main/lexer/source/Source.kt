package lexer.source

interface Source {
    // returns:
    // - null if the end of text was reached
    // - char otherwise
    fun getChar(): Char?
}
