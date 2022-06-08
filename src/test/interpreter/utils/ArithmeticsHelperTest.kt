package interpreter.utils

import interpreter.exception.CombinationOfUnsupportedValueTypesException
import interpreter.exception.DifferentTypesComparisonException
import interpreter.exception.DivisionByZeroException
import interpreter.model.Currency
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class ArithmeticsHelperTest : WordSpec({
    ArithmeticsHelper::class.java.simpleName should {
        "add values" {
            forAll(
                row(3, 5, 8, Int::class.simpleName),
                row(3.0, 5.0, 8.0, Double::class.simpleName),
                row("3", "5", "35", String::class.simpleName),
                row(Currency(BigDecimal(3), "PLN"), Currency(BigDecimal(5), "PLN"), Currency(BigDecimal(8), "PLN"), Currency::class.simpleName)
            ) {
                    val1, val2, res, className ->
                ArithmeticsHelper.add(val1, val2).apply {
                    this.toString() shouldBe res.toString()
                    this::class.simpleName shouldBe className
                }
            }
        }

        "subtract values" {
            forAll(
                row(3, 5, -2, Int::class.simpleName),
                row(3.0, 5.0, -2.0, Double::class.simpleName),
                row(Currency(BigDecimal(3), "PLN"), Currency(BigDecimal(5), "PLN"), Currency(BigDecimal(-2), "PLN"), Currency::class.simpleName)
            ) {
                    val1, val2, res, className ->
                ArithmeticsHelper.subtract(val1, val2).apply {
                    this.toString() shouldBe res.toString()
                    this::class.simpleName shouldBe className
                }
            }
        }

        "multiply values" {
            forAll(
                row(3, 5, 15, Int::class.simpleName),
                row(3.0, 5.0, 15.0, Double::class.simpleName),
                row(3, 5.0, 15.0, Double::class.simpleName),
                row(Currency(BigDecimal(3), "PLN"), 5, Currency(BigDecimal(15), "PLN"), Currency::class.simpleName),
                row(Currency(BigDecimal(3), "PLN"), 5.0, Currency(BigDecimal(15.0), "PLN"), Currency::class.simpleName),
                row(Currency(BigDecimal(3.25), "PLN"), 5.0, Currency(BigDecimal(16.25), "PLN"), Currency::class.simpleName),
                row(5, Currency(BigDecimal(3), "PLN"), Currency(BigDecimal(15), "PLN"), Currency::class.simpleName),
                row(5.0, Currency(BigDecimal(3), "PLN"), Currency(BigDecimal(15.0), "PLN"), Currency::class.simpleName),
            ) {
                    val1, val2, res, className ->
                ArithmeticsHelper.multiply(val1, val2).apply {
                    this.toString() shouldBe res.toString()
                    this::class.simpleName shouldBe className
                }
            }
        }

        "divide values" {
            forAll(
                row(3, 5, 0, Int::class.simpleName),
                row(3.0, 5.0, 0.6, Double::class.simpleName),
                row(3, 5.0, 0.6, Double::class.simpleName),
                row(Currency(BigDecimal(3.00), "PLN"), 5.00, Currency(BigDecimal(0.60), "PLN"), Currency::class.simpleName),
                row(Currency(BigDecimal(3.00), "PLN"), 5.00, Currency(BigDecimal(0.60), "PLN"), Currency::class.simpleName),
                row(Currency(BigDecimal(5), "PLN"), Currency(BigDecimal(5), "PLN"), 1.0, Double::class.simpleName),
            ) {
                    val1, val2, res, className ->
                ArithmeticsHelper.divide(val1, val2).apply {
                    this.toString() shouldBe res.toString()
                    this::class.simpleName shouldBe className
                }
            }
        }

        "throw ${CombinationOfUnsupportedValueTypesException::class.simpleName} when combination of value types is not supported" {
            forAll(
                row(true, false, ArithmeticsHelper.Companion::add),
                row(true, false, ArithmeticsHelper.Companion::subtract),
                row("123", "12", ArithmeticsHelper.Companion::subtract),
                row(3, true, ArithmeticsHelper.Companion::multiply),
                row(true, 3, ArithmeticsHelper.Companion::multiply),
                row(3, "123", ArithmeticsHelper.Companion::multiply),
                row(3, Currency(BigDecimal(1), "PLN"), ArithmeticsHelper.Companion::divide),
                row(3.0, Currency(BigDecimal(1), "PLN"), ArithmeticsHelper.Companion::divide),
                row(3.0, true, ArithmeticsHelper.Companion::divide),
                row(Currency(BigDecimal(1), "PLN"), true, ArithmeticsHelper.Companion::divide),
                row(Currency(BigDecimal(1), "PLN"), "123", ArithmeticsHelper.Companion::divide)
            ) {
                    val1, val2, function ->
                shouldThrow<CombinationOfUnsupportedValueTypesException> { function(val1, val2) }
            }
        }

        "throw ${DifferentTypesComparisonException::class.simpleName} when acting on different value types" {
            forAll(
                row(1, 1.0, ArithmeticsHelper.Companion::add),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(1), "EUR"), ArithmeticsHelper.Companion::add),
                row(1, 1.0, ArithmeticsHelper.Companion::subtract),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(1), "EUR"), ArithmeticsHelper.Companion::subtract),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(1), "EUR"), ArithmeticsHelper.Companion::divide),
            ) {
                    val1, val2, function ->
                shouldThrow<DifferentTypesComparisonException> { function(val1, val2) }
            }
        }

        "throw ${DivisionByZeroException::class.simpleName} when dividing by 0" {
            forAll(
                row(1, 0),
                row(1.0, 0),
                row(1, 0.0),
                row(Currency(BigDecimal(1), "PLN"), 0),
                row(Currency(BigDecimal(1), "PLN"), 0.0),
                row(Currency(BigDecimal(1), "PLN"), Currency(BigDecimal(0), "PLN")),
            ) {
                    val1, val2 ->
                shouldThrow<DivisionByZeroException> { ArithmeticsHelper.divide(val1, val2) }
            }
        }
    }
})
