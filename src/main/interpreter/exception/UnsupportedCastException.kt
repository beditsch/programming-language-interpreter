package interpreter.exception

import parser.model.Type

class UnsupportedCastException(from: String, to: Type? = null) :
    Exception("Cannot cast from ${from}${to?.let { " to ${it.name}" } ?: "."}")
