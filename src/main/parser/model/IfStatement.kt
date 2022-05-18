package parser.model

class IfStatement(
    val condition: ConditionBase,
    val instruction: Instruction,
    val elseInstruction: Instruction?
) : Statement()
