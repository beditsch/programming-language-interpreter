package lexer.source

import java.io.File
import java.nio.charset.Charset

class FileSource(filePath: String) : Source {
    private var sourceFileReader = File(filePath).reader(Charset.defaultCharset())

    override fun getChar(): Char? {
        sourceFileReader.read().apply {
            return if (this == -1) null
            else this.toChar()
        }
    }
}
