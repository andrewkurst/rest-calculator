package com.example.calculator

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@OpenAPIDefinition(info = Info(title = "REST-калькулятор", version = "1.0.0"))
class CalculatorApplication

fun main(args: Array<String>) {
    runApplication<CalculatorApplication>(*args)
}
