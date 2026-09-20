# =========================
# BUILD STAGE
# =========================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Maven wrapper + pom
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw

# Исходники
COPY src src

# Сборка приложения
RUN ./mvnw clean package -DskipTests


# =========================
# RUNTIME STAGE
# =========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user
RUN addgroup -S bank && adduser -S bank -G bank

# Готовый jar из builder
COPY --from=builder /app/target/bank-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/logs && \
    chown bank:bank app.jar /app/logs

USER bank

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]




## Многоэтапная сборка (multi-stage build)
#
## ── Этап 1: Сборка ──────────────────────────────────────────────
#FROM eclipse-temurin:21-jdk-alpine AS builder
#WORKDIR /app
#
## Сначала копируем только pom.xml — чтобы зависимости кэшировались отдельно
#COPY pom.xml .
#COPY .mvn .mvn
#COPY mvnw .
#
## Скачиваем зависимости (этот слой будет кэшироваться если pom.xml не менялся)
#RUN ./mvnw dependency:go-offline -B
#
## Копируем исходный код и собираем
#COPY src ./src
#RUN ./mvnw package -DskipTests -B
#
## ── Этап 2: Runtime ─────────────────────────────────────────────
#FROM eclipse-temurin:21-jre-alpine
#WORKDIR /app
#
## Создаём непривилегированного пользователя (безопасность)
#RUN addgroup -S bank && adduser -S bank -G bank
#
## Копируем только собранный jar из этапа сборки
#COPY --from=builder /app/target/*.jar app.jar
#
## Меняем владельца файлов
#RUN chown -R bank:bank /app
#USER bank
#
## Документируем порт (не пробрасывает, только информация)
#EXPOSE 8080
#
## Переменные среды по умолчанию (можно переопределить при запуске)
#ENV SPRING_PROFILES_ACTIVE=prod \
#    JAVA_OPTS="-Xmx512m -Xms256m"
#
## Команда запуска
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]