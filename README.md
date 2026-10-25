# 🏦 Bank API

[![CI](https://github.com/El032/bank/actions/workflows/build.yml/badge.svg)](https://github.com/El032/bank/actions/workflows/build.yml)

REST API банковского приложения на **Java 21** и **Spring Boot**, разработанный как практический backend-проект с архитектурой, приближенной к реальному production-приложению.

Проект демонстрирует не только создание REST API, но и полный backend-цикл: аутентификация и авторизация, работа с PostgreSQL, миграции базы данных, Redis-кэширование, асинхронное взаимодействие через Apache Kafka, банковские операции, транзакции, банковские карты, ATM-сценарии, мониторинг, логирование, Docker, Kubernetes, CI/CD и deployment в production.

🌐 **Live:** https://bank-rh5e.onrender.com  
📚 **Swagger UI:** https://bank-rh5e.onrender.com/swagger-ui/index.html  
💻 **GitHub:** https://github.com/El032/bank  

---

# 🏗 Архитектура системы

```mermaid
flowchart TD
    Client(["Клиент / Frontend / Swagger UI"]) -->|REST API + JWT| Controller["Spring Boot Web Layer\n(REST Controllers, Security, Validation)"]

    subgraph App ["Bank Core Service"]
        Controller --> Service["Service Layer\n(Бизнес-логика, @Transactional)"]
        Service --> Cache[("Redis\n(Cache-Aside: Users, Balances)")]
        Service --> Repo["Spring Data JPA / Hibernate"]
        Service --> Outbox["Transactional Outbox Table"]
        
        Worker["Outbox Scheduler / Poller"] -->|Периодический опрос| Outbox
    end

    Repo --> DB[("PostgreSQL\n(Users, Accounts, Cards, Transactions)")]
    Worker -->|Публикация событий| Kafka{{"Apache Kafka\n(TransferCompletedEvent)"}}
    
    Kafka --> Consumers["Idempotent Consumers\n(ProcessedNotification)"]
    Consumers --> Mail["Email Service\n(JavaMailSender)"]

    subgraph Observability ["Monitoring & Metrics"]
        Actuator["Spring Actuator & Micrometer"] --> Prometheus[("Prometheus")]
        Prometheus --> Grafana["Grafana Dashboards"]
    end
    App -.-> Actuator
🚀 Возможности проекта🔐 Аутентификация и безопасностьРегистрация пользователейАутентификация через JWTSpring SecurityЗащита REST endpointsJWT authentication filterStateless authenticationРолевая модель USER / ADMINРазделение обычных и административных операцийMethod-level authorization через @PreAuthorizeBCrypt для хранения паролейПроверка доступа к ресурсам по владельцуБлокировка неактивных пользователейОбработка 401 Unauthorized и 403 ForbiddenCustom AccessDeniedHandler👤 ПользователиСоздание пользователейПолучение списка пользователейПолучение пользователя по IDПолучение счетов пользователяСоздание банковских счетов для пользователяИзменение статуса пользователяСвязь пользователя с банковскими счетамиПроверка доступа к пользователю по владельцуRedis-кэширование данных пользователейУправление статусом пользователя через Admin API💳 Банковские счетаПоддерживаются типы счетов:DEBITSAVINGSCREDITРеализованы:создание счетов;получение списка счетов;pagination;фильтрация по статусу;поиск по владельцу;получение информации по ID;получение истории транзакций;получение истории переводов;банковская выписка;пополнение;снятие средств;изменение статуса;проверка владельца счёта.Жизненный цикл счёта:
ACTIVE -> BLOCKED -> ACTIVE или ACTIVE / BLOCKED -> CLOSED (закрытый счёт не может быть повторно активирован).💸 ПереводыРеализованы переводы между банковскими счетами.При переводе создаются соответствующие транзакции:у отправителя — WITHDRAW + TRANSFER;у получателя — DEPOSIT + TRANSFER.Перевод выполняется в рамках @Transactional, что обеспечивает атомарность банковской операции.В рамках одной транзакции:проверяются счета отправителя и получателя;проверяется активность счетов;проверяются бизнес-ограничения;изменяются балансы;создаются записи истории транзакций;сохраняется информация о переводе;создаётся Outbox Event.После обработки Outbox-события:событие публикуется в Apache Kafka;выполняется обработка события consumer'ом;отправляется email-уведомление о переводе.Метрики переводов собираются через Micrometer:bank.transfers.activebank.transfers.amountbank.transfers.durationbank.transfers.failedbank.transfers.success🧾 ТранзакцииДля каждой финансовой операции сохраняется информация о:сумме;типе операции (TransactionType: DEPOSIT, WITHDRAW, INTEREST);источнике операции (TransactionSource: ATM, API, TRANSFER);времени создания;связанном банковском счёте.История транзакций поддерживает pagination.💳 Банковские картыРеализованы:создание банковских карт (DEBIT и CREDIT);запрет выпуска карт для SAVINGS-счетов;генерация номера карты, PIN и CVV;BCrypt-хеширование PIN;шифрование CVV;защищённое получение CVV;изменение PIN и проверка старого PIN владельцем;управление статусом карты (ACTIVE -> BLOCKED -> CLOSED).🏧 ATMСценарий работы с банкоматом:вставка банковской карты;создание ATM-сессии и проверка PIN;получение баланса, пополнение и снятие денежных средств;закрытие ATM-сессии и возврат карты;лимиты: минимальная сумма 100, дневной лимит 50 000;защита: для одной карты не допускается несколько активных ATM-сессий одновременно.🛠 Технологический стекКатегорияТехнологииЯзыкJava 21FrameworkSpring Boot 3.5.0WebSpring Web / RESTSecuritySpring Security + JWTORMSpring Data JPA + HibernateDatabasePostgreSQLМиграцииFlywayCacheRedis + Spring CacheMessagingApache KafkaEmailSpring MailAPI DocumentationSpringDoc OpenAPI / SwaggerMonitoringSpring Boot Actuator + MicrometerMetricsPrometheusDashboardsGrafanaLoggingSLF4J / Logback + MDCTestingJUnit 5 / Mockito / TestcontainersContainerizationDocker / Docker ComposeOrchestrationKubernetes / MinikubeCI/CDGitHub ActionsProductionRender📨 Apache Kafka & Transactional OutboxДля асинхронного взаимодействия между компонентами используется Apache Kafka.
События: TransferCompletedEvent, AccountCreatedEvent.Transactional OutboxПосле успешного перевода событие сначала сохраняется в Outbox в рамках той же транзакции, что и банковская операция.
Затем отдельный процесс:получает необработанное событие;помечает его как processing;публикует событие в Kafka;после успешной публикации помечает его как published.
Также реализовано восстановление зависших Outbox-событий.IdempotencyДля уведомлений используется ProcessedNotification. Перед обработкой события проверяется, не было ли оно уже обработано. Это предотвращает повторную отправку писем и начислений.⚡ RedisRedis используется как внешний distributed cache layer:Spring Cache и RedisCacheManager;кэширование пользователей и данных счетов;ручной Cache-Aside для банковских счетов;TTL: users — 15 минут, users-list — 15 минут, accounts — 5 минут, default — 10 минут;Redis health check через Actuator.🗄 Работа с базой данныхОсновная база данных проекта — PostgreSQL:Spring Data JPA, Hibernate, HikariCP;Flyway миграции (от V1 до V27);Решение N+1: JOIN FETCH, LEFT JOIN FETCH, @EntityGraph;ddl-auto=validate в production.🔄 Transaction ManagementФинансовые операции выполняются в рамках транзакций Spring (@Transactional).
В рамках одной транзакции атомарно изменяются балансы обоих счетов, пишется история транзакций и создается Outbox Event. При сбое происходит полный rollback.📊 Мониторинг и ObservabilityEndpoints: /actuator/health, /actuator/info, /actuator/metrics, /actuator/prometheus;Custom Health Indicators: BankHealthIndicator, DatabaseHealthIndicator;Метрики JVM, GC, HikariCP, HTTP requests, Kafka и бизнес-метрики переводов;Дашборды в Grafana.📝 Логирование и Request IDСтруктурированное логирование HTTP-запросов через MDC-контекст:
[61781daa] RequestLoggingFilter - GET /auth/me -> 200 (8ms)
Request ID позволяет связать HTTP-запрос с логами приложения и SQL-запросами.🧪 ТестированиеПроект содержит 450 автоматических тестов:Tests run: 450, Failures: 0, Errors: 0, Skipped: 0;Стек: JUnit 5, Mockito, Spring Boot Test, Testcontainers (PostgreSQL);Анализ покрытия через JaCoCo.🐳 Docker & ☸️ KubernetesMulti-stage Dockerfile:Build stage: eclipse-temurin:21-jdk-alpineRuntime stage: eclipse-temurin:21-jre-alpine (non-root пользователь bank).Docker Compose сервисы:Bank API (:8080)PostgreSQL 16 (:5432)Redis 7 (:6379)Apache Kafka 7.6 + ZooKeeper (:9092)Prometheus (:9090)Grafana (:3000)Kubernetes (Minikube):Bank API Deployment (2 реплики, RollingUpdate, startup/readiness/liveness probes)Stateful Deployments, Services (ClusterIP, NodePort), ConfigMap, Secrets, PVC.🔁 CI/CD & Production DeploymentGitHub Actions:При push и PR в main запускается сборка и прогон 450 тестов.Автоматический деплой на Render через Webhook.При создании тега v* собирается JAR, создается GitHub Release и публикуется образ в ghcr.io/el032/bank.📚 API DocumentationИнтерактивная документация: https://bank-rh5e.onrender.com/swagger-ui/index.htmlEndpoints:POST /auth/register, POST /auth/login, GET /auth/meGET /users, GET /users/{id}, GET /users/{id}/accounts, POST /users, POST /users/{id}/accountsGET /accounts, GET /accounts/{id}, GET /accounts/search, PATCH /accounts/{id}, GET /accounts/{id}/transactions, GET /accounts/{id}/transfers, GET /accounts/{id}/statementPOST /transfersPOST /accounts/{id}/cards, GET /accounts/{id}/cards, GET /accounts/{id}/cards/cvv, PATCH /accounts/{id}/cards/pinPOST /atm/cards, POST /atm/sessions/{sessionId}/pin, GET /atm/sessions/{sessionId}/balance, POST /atm/sessions/{sessionId}/deposit, POST /atm/sessions/{sessionId}/withdraw, POST /atm/sessions/{sessionId}/closePATCH /admin/users/{id}/status, PATCH /admin/accounts/{id}/status, POST /admin/accounts/{id}/deposit, POST /admin/accounts/{id}/withdraw, PATCH /admin/cards/{id}/status🔐 Environment VariablesЧувствительные данные передаются через переменные окружения (.env.example):
DATABASE_URL, DATABASE_USER, DATABASE_PASS, BANK_JWT_SECRET, BANK_JWT_EXPIRATION_MS, CVV_ENCRYPTION_KEY, MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD, BANK_NOTIFICATION_EMAIL, SPRING_KAFKA_BOOTSTRAP_SERVERS, KAFKA_TRUSTSTORE_CERTIFICATE.▶️ Быстрый локальный стартgit clone https://github.com/El032/bank.git
cd bank
docker compose up -dAPI & Swagger: http://localhost:8080/swagger-ui/index.htmlHealth Check: http://localhost:8080/actuator/health
