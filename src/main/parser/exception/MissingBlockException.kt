package parser.exception

import shared.Position

class MissingBlockException(
    parsingFunctionName: String,
    position: Position?
) : Exception("Missing block while performing $parsingFunctionName at position: $position.")
