package parser.exception

import shared.Position

class MissingParameterException(
    position: Position?
) : Exception("Missing function parameter at position: $position.")
