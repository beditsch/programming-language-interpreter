package lexer.source

import java.nio.charset.Charset

class StringSource(s: String) : Source {
    private val inputStream = s.byteInputStream(charset = Charset.defaultCharset())

    override fun getChar(): Char? {
        inputStream.read().apply {
            return if (this == -1) null
            else this.toChar()
        }
    }
}
