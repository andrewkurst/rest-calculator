package com.example.calculator

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

data class CalculationResult(val result: BigDecimal)
data class ApiError(val error: String)

@RestController
class CalculatorController(private val calculator: CalculatorService) {
    @Operation(
        summary = "Выполнить арифметическую операцию",
        description = "Десятичные числа до 100 символов. Деление округляется до 34 значащих цифр.",
        responses = [
            ApiResponse(responseCode = "200", description = "Результат вычисления"),
            ApiResponse(
                responseCode = "400", description = "Неверные параметры",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @GetMapping("/calculate", produces = ["application/json"])
    fun calculate(
        @Parameter(description = "Первое число", example = "10", schema = Schema(maxLength = 100))
        @RequestParam a: String,
        @Parameter(description = "Второе число", example = "5", schema = Schema(maxLength = 100))
        @RequestParam b: String,
        @Parameter(
            description = "Операция. В URL знак + кодируется как %2B.", example = "+",
            schema = Schema(allowableValues = ["+", "-", "*", "/"])
        )
        @RequestParam operation: String,
        @Parameter(hidden = true) @RequestParam params: MultiValueMap<String, String>
    ): CalculationResult {
        require(params.keys.all { it in setOf("a", "b", "operation") }) {
            "Allowed parameters: a, b, operation"
        }
        require(params.values.all { it.size == 1 }) { "Duplicate query parameter" }
        return CalculationResult(calculator.calculate(a, b, operation))
    }
}
