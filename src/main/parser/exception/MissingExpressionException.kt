package parser.exception

import shared.Position

class MissingExpressionException(
    parsingFunctionName: String,
    position: Position?
) : Exception("Missing expression while executing $parsingFunctionName at position: $position.")
