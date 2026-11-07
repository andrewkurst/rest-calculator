package com.example.calculator

import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.net.URI

@SpringBootTest
@AutoConfigureMockMvc
class CalculatorApiTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun `all operations and decimals work`() {
        val cases = listOf(
            listOf("10", "5", "+", "15"), listOf("10", "5", "-", "5"),
            listOf("10", "5", "*", "50"), listOf("10", "5", "/", "2"),
            listOf("0.1", "0.2", "+", "0.3"), listOf("-6", "4", "/", "-1.5")
        )
        for ((a, b, operation, expected) in cases) {
            mvc.perform(get("/calculate").param("a", a).param("b", b).param("operation", operation))
                .andExpect(status().isOk)
                .andExpect(content().json("""{"result":$expected}"""))
        }
        mvc.perform(get(URI.create("/calculate?a=10&b=5&operation=%2B")))
            .andExpect(status().isOk).andExpect(jsonPath("$.result").value(15))
        mvc.perform(get("/calculate").param("a", "1").param("b", "3").param("operation", "/"))
            .andExpect(status().isOk)
            .andExpect(content().string("""{"result":0.3333333333333333333333333333333333}"""))
    }

    @Test
    fun `division by zero returns a clear bad request error`() {
        for (zero in listOf("0", "+0", "-0", "0.0", "-0.0", ".0", "00.00")) {
            for (dividend in listOf("1", "0")) {
                mvc.perform(get("/calculate").param("a", dividend).param("b", zero).param("operation", "/"))
                    .andExpect(status().isBadRequest)
                    .andExpect(content().contentTypeCompatibleWith("application/json"))
                    .andExpect(content().json("""{"error":"Делить на ноль нельзя"}"""))
            }
        }
    }

    @Test
    fun `zero remains valid for other operations and as a dividend`() {
        for (operation in listOf("+", "-", "*")) {
            mvc.perform(get("/calculate").param("a", "1").param("b", "0").param("operation", operation))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value(if (operation == "*") 0 else 1))
        }
        mvc.perform(get("/calculate").param("a", "0").param("b", "2").param("operation", "/"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value(0))
    }

    @Test
    fun `invalid inputs return JSON errors`() {
        for (query in listOf(
            "a=NaN&b=2&operation=*", "a=abc&b=2&operation=*",
            "a=1&operation=*", "a=1&b=2", "a=1&b=2&operation=unknown",
            "a=1&a=2&b=3&operation=*", "a=1&b=2&operation=*&extra=1", "",
            "a=${"1".repeat(101)}&b=2&operation=*"
        )) {
            mvc.perform(get(URI.create("/calculate?$query")))
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.error").isString)
        }
    }

    @Test
    fun `routing and methods are checked`() {
        mvc.perform(post("/calculate"))
            .andExpect(status().isMethodNotAllowed)
            .andExpect(header().string("Allow", "GET"))
            .andExpect(jsonPath("$.error").isString)
        mvc.perform(get("/missing")).andExpect(status().isNotFound)
    }

    @Test
    fun `openapi is generated from controller`() {
        mvc.perform(get("/openapi.json"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.info.title").value("REST-калькулятор"))
            .andExpect(jsonPath("$.paths['/calculate'].get.parameters[*].name", hasItems("a", "b", "operation")))
            .andExpect(jsonPath("$.paths['/calculate'].get.parameters[?(@.name == 'operation')].schema.enum[0]").value("+"))
            .andExpect(jsonPath("$.paths['/calculate'].get.responses['400']").exists())
    }

    @Test
    fun `swagger UI and its assets are served locally`() {
        mvc.perform(get("/swagger-ui"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/swagger-ui/index.html"))
        mvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith("text/html"))
        mvc.perform(get("/swagger-ui/swagger-ui-bundle.js")).andExpect(status().isOk)
        mvc.perform(get("/openapi.json/swagger-config"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.url").value("/openapi.json"))
    }
}
