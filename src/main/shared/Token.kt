package shared

class Token<ValueType>(
    var tokenType: TokenType,
    var position: Position,
    var value: ValueType? = null
)
