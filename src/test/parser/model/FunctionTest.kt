package parser.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.string.shouldContain
import parser.exception.DuplicateFunctionParameterIdentifierException

class FunctionTest : WordSpec({
    Function::class.java.simpleName should {
        "throw ${DuplicateFunctionParameterIdentifierException::class.simpleName} when two parameters have the same identifier" {
            val functionIdentifier = "myFunc"
            val duplicatedParamIdentifier = "param1"
            val params = listOf(
                Parameter(null, duplicatedParamIdentifier),
                Parameter(null, "param2"),
                Parameter(null, duplicatedParamIdentifier)
            )
            shouldThrow<DuplicateFunctionParameterIdentifierException> {
                Function(VariableType(Type.INT), functionIdentifier, params, Block(emptyList()))
            }.apply {
                message shouldContain functionIdentifier
                message shouldContain duplicatedParamIdentifier
            }
        }
    }
})
