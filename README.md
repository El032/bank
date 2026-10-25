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
