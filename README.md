### Краткое описание
Проект из 2х модулей:
* product-service
* payment-service

Для запуска проекта достаточно собрать его с помощью gradle.

* http://localhost:8080/products - открывает витрину товаров (можно бегать по ссылкам)
* http://localhost:8081/swagger-ui.html - сваггер для проверки ендпоинтов сервиса платежей
* http://localhost:8083/ - keycloak ui

Запуск keycloak производится через `docker-compose up --build` команду. docker-compose.yaml лежит в корне проекта.

### Настройка keycloak
Создать реалм _shop_

Создать два клиента:
* product-service-client
* payment-service-client

#### product-service-client:
* **Root Url** = http://localhost:8080
* **Valid redirect URIs** = http://localhost:8080/*
* **Valid post logout redirect URIs** = http://localhost:8080/*
* **Client Authentication** = on
* **Authorization** = on
* **Authentication flow** = Standard flow, Direct access grants, Service account roles