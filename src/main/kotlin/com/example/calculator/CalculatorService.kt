package com.example.calculator

import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.MathContext

@Service
class CalculatorService {
    fun calculate(a: String, b: String, operation: String): BigDecimal {
        val first = number(a)
        val second = number(b)
        return when (operation) {
            "+" -> first + second
            "-" -> first - second
            "*" -> first * second
            "/" -> {
                require(second.signum() != 0) { "Делить на ноль нельзя" }
                first.divide(second, MathContext.DECIMAL128)
            }
            else -> throw IllegalArgumentException("operation must be +, -, *, /")
        }
    }

    private fun number(value: String): BigDecimal {
        require(value.length <= 100 && DECIMAL.matches(value)) {
            "a and b must be decimal numbers (up to 100 characters)"
        }
        return value.toBigDecimal()
    }

    private companion object {
        val DECIMAL = Regex("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)")
    }
}
