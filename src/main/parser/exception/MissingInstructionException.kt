package parser.exception

import shared.Position

class MissingInstructionException(
    parsingFunctionName: String,
    position: Position?
) : Exception("Missing instruction while performing $parsingFunctionName at position: $position")
