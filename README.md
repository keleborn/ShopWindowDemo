Проект из 2х модулей:
* product-service
* payment-service

Для запуска проекта достаточно собрать его с помощью gradle.

* http://localhost:8080/products - открывает витрину товаров (можно бегать по ссылкам)
* http://localhost:8081/swagger-ui.html - сваггер для проверки ендпоинтов сервиса платежей
* http://localhost:8083/ - keycloak ui

Запуск keycloak производится через `docker-compose up --build` команду. docker-compose.yaml лежит в корне проекта.
