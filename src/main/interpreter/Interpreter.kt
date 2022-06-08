package interpreter

import parser.model.Program

class Interpreter(
    program: Program,
    currencyMap: Map<String, Map<String, Double>>,
    val visitor: Visitor = Visitor(program, currencyMap)
) {
    companion object {
        private const val MAIN_FUNCTION_ID = "main"
    }
    fun interpret() {
        val result = visitor.executeProgram(MAIN_FUNCTION_ID)
        print("Program execution result: $result")
    }
}
