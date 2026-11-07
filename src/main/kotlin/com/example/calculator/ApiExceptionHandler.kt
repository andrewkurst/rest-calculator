package com.example.calculator

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun invalidArgument(exception: IllegalArgumentException): ResponseEntity<ApiError> =
        ResponseEntity.badRequest().body(ApiError(exception.message ?: "Invalid parameters"))

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun missingParameter(): ResponseEntity<ApiError> =
        ResponseEntity.badRequest().body(ApiError("Required parameters: a, b, operation"))

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun unsupportedMethod(exception: HttpRequestMethodNotSupportedException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .allow(*(exception.supportedHttpMethods ?: emptySet()).toTypedArray())
            .body(ApiError("Method not allowed"))
}
