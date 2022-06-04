package parser.exception

import shared.Position

class MissingConditionException(
    parsingFunctionName: String,
    position: Position?
) : Exception("Missing condition while performing $parsingFunctionName at position: $position.")
