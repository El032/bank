# 🏦 Bank API

[![CI](https://github.com/El032/bank/actions/workflows/build.yml/badge.svg)](https://github.com/El032/bank/actions/workflows/build.yml)

REST API банковского приложения на **Java 21** и **Spring Boot**, разработанный как практический backend-проект с архитектурой, приближенной к реальному production-приложению.

Проект демонстрирует не только создание REST API, но и полный backend-цикл: аутентификация и авторизация, работа с PostgreSQL, миграции базы данных, Redis-кэширование, асинхронное взаимодействие через Apache Kafka, банковские операции, транзакции, банковские карты, ATM-сценарии, мониторинг, логирование, Docker, Kubernetes, CI/CD и deployment в production.

🌐 **Live:** https://bank-rh5e.onrender.com
📚 **Swagger UI:** https://bank-rh5e.onrender.com/swagger-ui/index.html
💻 **GitHub:** https://github.com/El032/bank

---

# 🚀 Возможности проекта

## 🔐 Аутентификация и безопасность

* Регистрация пользователей
* Аутентификация через JWT
* Spring Security
* Защита REST endpoints
* JWT authentication filter
* Stateless authentication
* Ролевая модель `USER` / `ADMIN`
* Разделение обычных и административных операций
* Method-level authorization через `@PreAuthorize`
* BCrypt для хранения паролей
* Проверка доступа к ресурсам по владельцу
* Блокировка неактивных пользователей
* Обработка `401 Unauthorized` и `403 Forbidden`
* Custom `AccessDeniedHandler`

---

## 👤 Пользователи

* Создание пользователей
* Получение списка пользователей
* Получение пользователя по ID
* Получение счетов пользователя
* Создание банковских счетов для пользователя
* Изменение статуса пользователя
* Связь пользователя с банковскими счетами
* Проверка доступа к пользователю по владельцу
* Redis-кэширование данных пользователей
* Управление статусом пользователя через Admin API

---

## 💳 Банковские счета

Поддерживаются типы счетов:

* `DEBIT`
* `SAVINGS`
* `CREDIT`

Реализованы:

* создание счетов;
* получение списка счетов;
* pagination;
* фильтрация по статусу;
* поиск по владельцу;
* получение информации по ID;
* получение истории транзакций;
* получение истории переводов;
* банковская выписка;
* пополнение;
* снятие средств;
* изменение статуса;
* проверка владельца счёта.

### Жизненный цикл счёта

```text
ACTIVE → BLOCKED → ACTIVE
```

или

```text
ACTIVE / BLOCKED → CLOSED
```

Закрытый счёт не может быть повторно активирован.

---

## 💸 Переводы

Реализованы переводы между банковскими счетами.

При переводе создаются соответствующие транзакции:

* у отправителя — `WITHDRAW + TRANSFER`;
* у получателя — `DEPOSIT + TRANSFER`.

Перевод выполняется в рамках `@Transactional`, что обеспечивает атомарность банковской операции.

### В рамках одной транзакции

* проверяются счета отправителя и получателя;
* проверяется активность счетов;
* проверяются бизнес-ограничения;
* изменяются балансы;
* создаются записи истории транзакций;
* сохраняется информация о переводе;
* создаётся `Outbox Event`.

### После обработки Outbox-события

* событие публикуется в Apache Kafka;
* выполняется обработка события consumer'ом;
* отправляется email-уведомление о переводе.

### 📊 Метрики переводов

С помощью Micrometer собираются:

* количество успешных переводов;
* количество неуспешных переводов;
* сумма переводов;
* длительность переводов;
* количество активных переводов.

---

## 🧾 Транзакции

Для каждой финансовой операции сохраняется информация о:

* сумме;
* типе операции;
* источнике операции;
* времени создания;
* связанном банковском счёте.

### Тип операции — `TransactionType`

* `DEPOSIT`
* `WITHDRAW`
* `INTEREST`

### Источник операции — `TransactionSource`

* `ATM`
* `API`
* `TRANSFER`

История транзакций поддерживает pagination.

Разделение типа и источника позволяет определить:

* **что произошло с деньгами** — тип операции;
* **в каком контексте была выполнена операция** — источник операции.

Например, при банковском переводе:

* у отправителя создаётся `WITHDRAW + TRANSFER`;
* у получателя создаётся `DEPOSIT + TRANSFER`.

---

## 💳 Банковские карты

Реализованы:

* создание банковских карт;
* поддержка `DEBIT` и `CREDIT`;
* запрет выпуска карт для `SAVINGS`-счетов;
* генерация номера карты;
* генерация PIN;
* генерация CVV;
* BCrypt-хеширование PIN;
* шифрование CVV;
* защищённое получение CVV;
* изменение PIN;
* проверка старого PIN при изменении владельцем;
* изменение PIN администратором;
* управление статусом карты.

### Жизненный цикл карты

```text
ACTIVE → BLOCKED → ACTIVE
```

или

```text
ACTIVE / BLOCKED → CLOSED
```

Закрытая карта не может быть повторно активирована.

---

## 🏧 ATM

Реализован отдельный сценарий работы с банкоматом:

* вставка банковской карты;
* создание ATM-сессии;
* проверка PIN;
* получение баланса;
* пополнение;
* снятие денежных средств;
* закрытие ATM-сессии;
* возврат карты.

### Дополнительные правила

* минимальная сумма операции — 100;
* дневной лимит ATM-снятия — 50 000;
* ATM-операции записываются с источником `ATM`;
* для одной карты не допускается несколько активных ATM-сессий одновременно.

ATM-операции также отражаются в истории транзакций с соответствующим источником `ATM`.

---

# 🏗 Архитектура

Проект построен по классической многоуровневой архитектуре:

```text
                    Client
                       │
                       ▼
              ┌─────────────────┐
              │ REST Controllers │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │    Services     │
              │  Business Logic │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │   Repositories  │
              │ Spring Data JPA │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │   PostgreSQL    │
              └─────────────────┘
```

Основной принцип взаимодействия:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Контроллеры отвечают за HTTP API, сервисы содержат бизнес-логику, а репозитории работают с данными.

### Инфраструктурные компоненты

```text
                       ┌──────────────┐
                       │    Redis     │
                       │    Cache     │
                       └──────┬───────┘
                              │
                              ▼
Client → Spring Boot → PostgreSQL
             │
             ├── JWT / Spring Security
             │
             ├── Apache Kafka
             │
             ├── Transactional Outbox
             │
             ├── Email notifications
             │
             └── Micrometer / Actuator
                       │
                       ├── Prometheus
                       └── Grafana
```

### Асинхронная обработка переводов

```text
TransferService
      ↓
OutboxEvent
      ↓
OutboxEventProcessor
      ↓
BankEventPublisher
      ↓
Apache Kafka
      ↓
NotificationConsumer
      ↓
ProcessedNotification
      ↓
EmailService
      ↓
JavaMailSender
```

---

# 🛠 Технологический стек

| Категория         | Технологии                         |
| ----------------- | ---------------------------------- |
| Язык              | Java 21                            |
| Framework         | Spring Boot 3.5.0                  |
| Web               | Spring Web / REST                  |
| Security          | Spring Security + JWT              |
| ORM               | Spring Data JPA + Hibernate        |
| Database          | PostgreSQL                         |
| Миграции          | Flyway                             |
| Cache             | Redis + Spring Cache               |
| Messaging         | Apache Kafka                       |
| Email             | Spring Mail                        |
| API Documentation | SpringDoc OpenAPI / Swagger        |
| Monitoring        | Spring Boot Actuator + Micrometer  |
| Metrics           | Prometheus                         |
| Dashboards        | Grafana                            |
| Logging           | SLF4J / Logback + MDC              |
| Testing           | JUnit 5 / Mockito / Testcontainers |
| Containerization  | Docker / Docker Compose            |
| Orchestration     | Kubernetes / Minikube              |
| CI/CD             | GitHub Actions                     |
| Production        | Render                             |

---

# 📨 Apache Kafka

Для асинхронного взаимодействия между компонентами используется Apache Kafka.

В проекте реализованы события:

* `TransferCompletedEvent`
* `AccountCreatedEvent`

Kafka используется совместно с **Transactional Outbox Pattern**.

## Transactional Outbox

После успешного перевода событие сначала сохраняется в Outbox в рамках той же транзакции, что и банковская операция.

Затем отдельный процесс:

* получает необработанное событие;
* помечает его как `processing`;
* публикует событие в Kafka;
* после успешной публикации помечает его как `published`.

Также реализовано восстановление зависших Outbox-событий.

## Idempotency

Для уведомлений используется `ProcessedNotification`.

Перед обработкой события проверяется, не было ли оно уже обработано.

Это предотвращает повторную обработку одного и того же уведомления.

## Kafka Consumer

Consumer:

* использует consumer group;
* обрабатывает `TransferCompletedEvent`;
* логирует partition и offset;
* при ошибке выбрасывает исключение для возможности повторной доставки.

После успешного перевода пользователь получает email-уведомление.

---

# ⚡ Redis

Redis используется как внешний distributed cache layer.

Реализованы:

* Spring Cache;
* `RedisCacheManager`;
* кэширование пользователей;
* кэширование списка пользователей;
* кэширование данных счетов;
* разные TTL для разных типов данных;
* JSON serialization;
* отключение caching `null`;
* cache invalidation;
* `RedisTemplate`;
* ручной Cache-Aside для банковских счетов;
* Redis health check через Actuator.

### Основные TTL

* `users` — 15 минут;
* `users-list` — 15 минут;
* `accounts` — 5 минут;
* default cache TTL — 10 минут.

---

# 🗄 Работа с базой данных

Основная база данных проекта — **PostgreSQL**.

Для работы с БД используются:

* Spring Data JPA;
* Hibernate;
* HikariCP;
* Flyway;
* JPQL;
* `@Query`;
* Native SQL;
* pagination;
* aggregation queries;
* Foreign Keys;
* UNIQUE constraints;
* database indexes.

## Оптимизация запросов

Для предотвращения N+1 используются:

* `JOIN FETCH`;
* `LEFT JOIN FETCH`;
* `@EntityGraph`.

Также реализованы:

* `COUNT`;
* `SUM`;
* фильтрация;
* сортировка;
* pagination;
* поиск по связанным сущностям.

## Database migrations

Схема базы данных управляется через Flyway.

В проекте присутствуют миграции от `V1` до `V27`.

В production:

```text
ddl-auto=validate
```

Hibernate не изменяет production schema автоматически, а проверяет соответствие JPA-моделей существующей структуре базы данных.

---

# 🔄 Transaction Management

Финансовые операции выполняются в рамках транзакций Spring.

Для критических операций используется:

```text
@Transactional
```

Это обеспечивает атомарность связанных изменений.

Например, перевод:

```text
Account A
   │
   ├── withdraw
   │
   ▼
Transaction


Account B
   │
   ├── deposit
   │
   ▼
Transaction
```

В рамках одной транзакции также сохраняются:

* перевод;
* история транзакций;
* Outbox Event.

При возникновении исключения выполняется rollback транзакции.

Transactional Outbox обеспечивает согласованное сохранение банковской операции и события для последующей публикации в Kafka.

---

# 📊 Мониторинг и Observability

Для monitoring и observability используются:

* Spring Boot Actuator;
* Micrometer;
* Prometheus;
* Grafana.

Реализованы собственные Health Indicators:

* `BankHealthIndicator`;
* `DatabaseHealthIndicator`.

### Production Actuator

Доступны:

```text
/actuator/health
/actuator/info
/actuator/metrics
/actuator/prometheus
```

Health-check контролирует состояние:

* Bank Health;
* PostgreSQL / Database;
* Redis;
* Disk Space;
* SSL;
* Liveness;
* Readiness.

### Business Metrics

Реализованы метрики:

```text
bank.accounts.created
bank.accounts.cards.created
bank.users.created

bank.transfers.active
bank.transfers.amount
bank.transfers.duration
bank.transfers.failed
bank.transfers.success
```

Также доступны стандартные Spring Boot / Micrometer metrics:

* JVM memory;
* Garbage Collector;
* JVM threads;
* CPU;
* process uptime;
* HTTP requests;
* HikariCP;
* JDBC connections;
* Redis / Lettuce;
* Kafka;
* Spring Security;
* Tomcat;
* cache;
* scheduled tasks.

Метрики доступны для анализа через Prometheus и Grafana.

---

# 📝 Логирование и Request ID

В приложении реализовано структурированное логирование HTTP-запросов.

Для каждого запроса используется `requestId`, который хранится в MDC-контексте.

Пример:

```text
[61781daa] RequestLoggingFilter - GET /auth/me → 200 (8ms)
```

Request ID позволяет связать HTTP-запрос с соответствующими логами приложения.

Логирование позволяет отслеживать:

* HTTP method;
* endpoint;
* HTTP status;
* время выполнения;
* authenticated user;
* authorities;
* JWT authentication;
* SQL-запросы при необходимости.

---

# 🧪 Тестирование

Проект содержит **450 автоматических тестов**.

Последний локальный запуск:

```text
Tests run: 450
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Используются:

* JUnit 5;
* Mockito;
* Spring Boot Test;
* Spring Security Test;
* Testcontainers;
* Testcontainers PostgreSQL;
* integration tests;
* service tests;
* repository tests;
* controller tests;
* event tests;
* security tests;
* Actuator / Metrics tests.

### JaCoCo

Для проекта настроен JaCoCo.

Во время Maven build генерируется отчёт покрытия тестами.

---

# 🐳 Docker

Приложение контейнеризовано с использованием Docker.

Используется **multi-stage Docker build**.

## Build stage

```text
eclipse-temurin:21-jdk-alpine
```

На этапе сборки выполняется Maven build.

## Runtime stage

```text
eclipse-temurin:21-jre-alpine
```

Приложение запускается от отдельного non-root пользователя `bank`.

Основной порт:

```text
8080
```

## Docker Compose

Docker Compose позволяет запускать локальную инфраструктуру:

* Bank API;
* PostgreSQL 16;
* Redis 7;
* Apache Kafka 7.6;
* ZooKeeper;
* Prometheus;
* Grafana.

Используются:

* Docker network;
* persistent volumes;
* healthchecks;
* service dependencies;
* restart policy.

Для development предусмотрен дополнительный `docker-compose-dev.yml` с remote debugging через порт `5005`.

---

# ☸️ Kubernetes

Проект разворачивался в Kubernetes с использованием Minikube для локального Kubernetes-окружения.

Подготовлены:

* Bank API Deployment;
* PostgreSQL Deployment;
* Redis Deployment;
* Kafka Deployment;
* Kubernetes Services;
* ConfigMap;
* Secret;
* PersistentVolumeClaim.

## Bank API

Bank API запускается в **2 репликах**.

Используется:

* Rolling Update;
* `maxUnavailable: 0`;
* `maxSurge: 1`;
* startup probe;
* readiness probe;
* liveness probe;
* resource requests;
* resource limits.

## Kafka

Kafka в Kubernetes работает в KRaft режиме.

## Storage

PostgreSQL использует PersistentVolumeClaim.

Redis работает с AOF persistence.

## Networking

Внутренние сервисы используют Kubernetes `ClusterIP`.

Bank API публикуется через `NodePort`.

---

# 🔁 CI/CD

Для автоматизации CI/CD используется GitHub Actions.

## Continuous Integration

При:

* `push` в `main`;
* `pull_request` в `main`

запускается workflow:

```text
git push
    │
    ▼
GitHub
    │
    ▼
GitHub Actions
    │
    ├── Checkout
    ├── Java 21
    ├── Maven
    └── mvn clean test
            │
            ▼
       450 tests
            │
            ▼
         SUCCESS
            │
            ▼
      Render Deploy Hook
            │
            ▼
       Production
```

После успешного прохождения CI запускается deployment через Render Deploy Hook.

## Release Pipeline

При создании Git tag формата:

```text
v*
```

запускается release workflow.

Например:

```text
v1.0.0
```

Release pipeline:

* собирает JAR;
* загружает JAR как GitHub Actions Artifact;
* создаёт GitHub Release;
* прикрепляет JAR к Release;
* собирает Docker image;
* публикует Docker image в GitHub Container Registry.

Docker images:

```text
ghcr.io/el032/bank:<version>
ghcr.io/el032/bank:latest
```

---

# 🚢 Production Deployment

Приложение развёрнуто в production на **Render**.

**Production URL:**

https://bank-rh5e.onrender.com

После успешного прохождения CI pipeline GitHub Actions запускает deployment через Render Deploy Hook.

Production environment использует:

* Java 21;
* PostgreSQL;
* Redis;
* Apache Kafka;
* JWT-аутентификацию;
* Flyway;
* Spring Boot Actuator;
* Prometheus metrics.

После deployment выполняется production smoke-test основных бизнес-сценариев, включая:

* регистрацию;
* аутентификацию;
* работу со счетами;
* переводы;
* историю транзакций.

---

# 📈 Production Monitoring

В production доступны следующие monitoring endpoints:

```text
/actuator/health
/actuator/info
/actuator/metrics
/actuator/prometheus
```

Health-check включает проверки:

* Bank Health;
* PostgreSQL / Database;
* Redis;
* Disk Space;
* SSL;
* Liveness;
* Readiness.

---

# 📚 API Documentation

Полная документация REST API доступна через Swagger UI:

https://bank-rh5e.onrender.com/swagger-ui/index.html

## Authentication

* `POST /auth/register`
* `POST /auth/login`
* `GET /auth/me`

## Users

* `GET /users`
* `GET /users/{id}`
* `GET /users/{id}/accounts`
* `POST /users`
* `POST /users/{id}/accounts`

## Accounts

* `GET /accounts`
* `GET /accounts/{id}`
* `GET /accounts/search`
* `PATCH /accounts/{id}`
* `GET /accounts/{id}/transactions`
* `GET /accounts/{id}/transfers`
* `GET /accounts/{id}/statement`

## Transfers

* `POST /transfers`

## Cards

* `POST /accounts/{id}/cards`
* `GET /accounts/{id}/cards`
* `GET /accounts/{id}/cards/cvv`
* `PATCH /accounts/{id}/cards/pin`

## ATM

* `POST /atm/cards`
* `POST /atm/sessions/{sessionId}/pin`
* `GET /atm/sessions/{sessionId}/balance`
* `POST /atm/sessions/{sessionId}/deposit`
* `POST /atm/sessions/{sessionId}/withdraw`
* `POST /atm/sessions/{sessionId}/close`

## Admin

* `PATCH /admin/users/{id}/status`
* `PATCH /admin/accounts/{id}/status`
* `POST /admin/accounts/{id}/deposit`
* `POST /admin/accounts/{id}/withdraw`
* `PATCH /admin/cards/{id}/status`

---

# 📁 Структура проекта

```text
bank/
├── .github/
│   └── workflows/
│       ├── build.yml
│       └── release.yml
│
├── k8s/
│   └── ...
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/bank/
│   │   │       ├── actuator/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── event/
│   │   │       ├── exception/
│   │   │       ├── filter/
│   │   │       ├── interceptor/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       └── service/
│   │   │
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/
│   │       ├── monitoring/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   │
│   └── test/
│       └── ...
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

# 🔐 Environment Variables

Секретные данные не хранятся в Git.

Для локального и production-запуска чувствительные параметры передаются через environment variables.

Основные переменные:

```text
DATABASE_URL=...
DATABASE_USER=...
DATABASE_PASS=...

BANK_JWT_SECRET=...
BANK_JWT_EXPIRATION_MS=...

CVV_ENCRYPTION_KEY=...

MAIL_HOST=...
MAIL_PORT=...
MAIL_USERNAME=...
MAIL_PASSWORD=...
BANK_NOTIFICATION_EMAIL=...

SPRING_KAFKA_BOOTSTRAP_SERVERS=...
KAFKA_TRUSTSTORE_CERTIFICATE=...
```

В production секретные значения хранятся в настройках deployment platform и GitHub Secrets.

Файл `.env.example` содержит шаблон переменных без реальных секретных значений.

---

# ▶️ Локальный запуск

## Требования

* Java 21
* Maven
* Docker
* Docker Compose

## Клонирование

```bash
git clone https://github.com/El032/bank.git
cd bank
```

## Вариант 1 — запуск всего приложения через Docker Compose

Docker Compose запускает:

* Bank API;
* PostgreSQL;
* Redis;
* Apache Kafka;
* ZooKeeper;
* Prometheus;
* Grafana.

```bash
docker compose up -d
```

После запуска:

```text
Application: http://localhost:8080
Swagger:     http://localhost:8080/swagger-ui/index.html
Health:      http://localhost:8080/actuator/health
```

## Вариант 2 — запуск Spring Boot локально

Инфраструктуру можно запустить через Docker Compose, а Spring Boot приложение — локально:

```bash
docker compose up -d postgres redis kafka zookeeper
```

Затем:

```bash
mvn spring-boot:run
```

Для локального запуска приложения используется соответствующий Spring profile и environment variables из `.env.example`.

---

# 🎯 Что демонстрирует проект

Этот проект демонстрирует практическое применение следующих backend-концепций:

* построение REST API;
* многослойная архитектура;
* Dependency Injection;
* DTO;
* Validation;
* Spring Data JPA;
* Hibernate;
* PostgreSQL;
* Flyway database migrations;
* транзакции;
* pagination;
* derived queries;
* JPQL и `@Query`;
* native SQL;
* оптимизация N+1;
* Spring Security;
* JWT;
* role-based authorization;
* проверка доступа по владельцу ресурса;
* блокировка неактивных пользователей;
* Redis caching;
* Apache Kafka;
* асинхронные события;
* Transactional Outbox Pattern;
* идемпотентная обработка уведомлений;
* email notifications;
* structured logging;
* MDC / requestId;
* Micrometer;
* Spring Boot Actuator;
* Prometheus;
* Grafana;
* Docker;
* Docker Compose;
* Kubernetes;
* Minikube;
* GitHub Actions;
* CI/CD;
* production deployment.


---

# 🌐 Project Links

**Live Application:**
https://bank-rh5e.onrender.com

**Swagger UI:**
https://bank-rh5e.onrender.com/swagger-ui/index.html

**GitHub:**
https://github.com/El032/bank
