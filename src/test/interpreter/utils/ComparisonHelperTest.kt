package interpreter.utils

import interpreter.exception.CombinationOfUnsupportedValueTypesException
import interpreter.exception.DifferentTypesComparisonException
import interpreter.model.Currency
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class ComparisonHelperTest : WordSpec({
    ComparisonHelper::class.java.simpleName should {
        "check equality" {
            forAll(
                row(1, 2, false),
                row(2, 2, true),
                row(1.1, 2.1, false),
                row(2.2, 2.2, true),
                row("12", "21", false),
                row("12", "12", true),
                row(false, true, false),
                row(false, false, true),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(3), "PLN"), false),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(1), "PLN"), true),
            ) {
                    val1, val2, result ->
                ComparisonHelper.equal(val1, val2) shouldBe result
            }
        }

        "check inequality" {
            forAll(
                row(1, 2, true),
                row(2, 2, false),
                row(1.1, 2.1, true),
                row(2.2, 2.2, false),
                row("12", "21", true),
                row("12", "12", false),
                row(false, true, true),
                row(false, false, false),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(3), "PLN"), true),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(1), "PLN"), false),
            ) {
                    val1, val2, result ->
                ComparisonHelper.notEqual(val1, val2) shouldBe result
            }
        }

        "check greater" {
            forAll(
                row(1, 2, false),
                row(2, 2, false),
                row(1.1, 2.1, false),
                row(2.2, 2.2, false),
                row("12", "21", false),
                row("12", "12", false),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(3), "PLN"), false),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(1), "PLN"), false),
            ) {
                    val1, val2, result ->
                ComparisonHelper.greater(val1, val2) shouldBe result
            }
        }

        "check less" {
            forAll(
                row(1, 2, true),
                row(2, 2, false),
                row(1.1, 2.1, true),
                row(2.2, 2.2, false),
                row("12", "21", true),
                row("12", "12", false),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(3), "PLN"), true),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(1), "PLN"), false),
            ) {
                    val1, val2, result ->
                ComparisonHelper.less(val1, val2) shouldBe result
            }
        }

        "check greater or equal" {
            forAll(
                row(1, 2, false),
                row(2, 2, true),
                row(1.1, 2.1, false),
                row(2.2, 2.2, true),
                row("12", "21", false),
                row("12", "12", true),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(3), "PLN"), false),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(1), "PLN"), true),
            ) {
                    val1, val2, result ->
                ComparisonHelper.greaterOrEqual(val1, val2) shouldBe result
            }
        }

        "check less or equal" {
            forAll(
                row(1, 2, true),
                row(2, 2, true),
                row(1.1, 2.1, true),
                row(2.2, 2.2, true),
                row("12", "21", true),
                row("12", "12", true),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(3), "PLN"), true),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(1), "PLN"), true),
            ) {
                    val1, val2, result ->
                ComparisonHelper.lessOrEqual(val1, val2) shouldBe result
            }
        }

        "throw ${DifferentTypesComparisonException::class.simpleName} when compared values are not of the same type" {
            forAll(
                row(1, 2.0),
                row(1, true),
                row(1, "1"),
                row(2.2, false),
                row("123", Currency(BigDecimal(1), "PLN")),
                row(2.0, Currency(BigDecimal(1), "PLN")),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(3), "EUR")),

            ) {
                    val1, val2 ->
                shouldThrow<DifferentTypesComparisonException> { ComparisonHelper.equal(val1, val2) }
            }
        }

        "throw ${CombinationOfUnsupportedValueTypesException::class.simpleName} when compared value types are not supported" {
            forAll(
                row(true, false),
                row(BigDecimal(1), BigDecimal(1)),
            ) {
                    val1, val2 ->
                shouldThrow<CombinationOfUnsupportedValueTypesException> { ComparisonHelper.greater(val1, val2) }
            }
        }
    }
})
