package parser.model

import interpreter.VisitorInterface

interface ProgramNode {
    fun acceptVisitor(visitor: VisitorInterface)
}
