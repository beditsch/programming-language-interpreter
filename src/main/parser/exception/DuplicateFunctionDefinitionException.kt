package parser.exception

import shared.Position

class DuplicateFunctionDefinitionException(
    funName: String,
    position: Position?
) : Exception("Duplicate definition of function: $funName found at position: $position.")
