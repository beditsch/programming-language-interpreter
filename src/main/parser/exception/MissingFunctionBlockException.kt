package parser.exception

import shared.Position

class MissingFunctionBlockException(
    funName: String,
    position: Position?
) : Exception("Missing function block for function: $funName at position: $position.")
