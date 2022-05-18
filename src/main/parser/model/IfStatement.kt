package parser.model

class IfStatement(
    private val condition: ConditionBase,
    private val instruction: Instruction,
    private val elseInstruction: Instruction?
) : Statement()
