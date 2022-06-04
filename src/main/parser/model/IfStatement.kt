package parser.model

class IfStatement(
    val condition: Expression,
    val instruction: Instruction,
    val elseInstruction: Instruction?
) : Statement()
