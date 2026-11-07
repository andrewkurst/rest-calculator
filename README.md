# REST-калькулятор

Kotlin + Spring Boot (Spring MVC), Gradle Kotlin DSL, Swagger UI через springdoc-openapi.
Требуется JDK 17+; Gradle поставляется через Wrapper.

## Запуск

Из корня проекта:

```sh
./gradlew bootRun
```

По умолчанию сервер доступен на `http://localhost:8080`. Другой порт можно передать аргументом:

```sh
./gradlew bootRun --args='--server.port=9090'
```

## Swagger

После запуска откройте [Swagger UI](http://localhost:8080/swagger-ui).
Раскройте `GET /calculate`, нажмите **Try it out**, задайте `a`, `b` и операцию, затем **Execute**.
Спецификация OpenAPI доступна по адресу `/openapi.json`. При смене порта используйте его и для Swagger UI.

OpenAPI генерируется из контроллера через [springdoc-openapi](https://springdoc.org/).
JS и CSS Swagger UI поставляются с приложением; доступ к CDN не нужен.

## API

`GET /calculate?a=10&b=5&operation=...`

Параметры: `a` — первое число, `b` — второе число, `operation` — `+`, `-`, `*` или `/`.
Поддерживаются отрицательные числа и десятичные дроби с точкой, до 100 символов в числе.
Вычисления используют `BigDecimal`; деление округляется до 34 значащих цифр. Экспоненциальная запись не поддерживается.

```sh
curl --get 'http://localhost:8080/calculate' \
  --data-urlencode 'a=10' \
  --data-urlencode 'b=5' \
  --data-urlencode 'operation=+'
```

Ответ: `{"result":15}`. Для остальных операций замените значение `operation` на `-`, `*` или `/`.
При ручном составлении URL знак `+` нужно кодировать как `%2B`.

Неверные числа, пропущенные, повторные или неизвестные параметры возвращают HTTP 400 и JSON с полем `error`.
Неизвестный путь возвращает 404, метод, отличный от GET, — 405.

## Сборка и тесты

Тесты на Kotlin проверяют операции и ошибки через Spring MockMvc, генерацию OpenAPI и доступность Swagger UI с его ресурсами:

```sh
./gradlew build
```

Для первой сборки нужен интернет, чтобы загрузить Gradle и зависимости.
Запуск готового приложения:

```sh
java -jar build/libs/rest-calculator-1.0.0.jar
```
