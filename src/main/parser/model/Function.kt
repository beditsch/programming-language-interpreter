package parser.model

import shared.Token

// TODO: wszystko powinno implementować nadrzędny interfejs Node
class Function(
    // TODO: wystarczy TokenType (parserowy)
    val funReturnType: Token<*>,
    val funIdentifier: String,
    val parameters: List<Parameter>,
    val functionBlock: Block
) : ProgramNode

// TODO: sprawdzenie, czy parametry nie mają powtórzeń
