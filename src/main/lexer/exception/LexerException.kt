package lexer.exception

import shared.Position
import java.lang.Exception

class LexerException(
    override var message: String,
    var position: Position
) : Exception(message)
