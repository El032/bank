.PHONY: up up-build down clean logs logs-all restart shell psql status

COMPOSE = docker compose -f src/main/resources/docker-compose.yml

# Поднять всё
up:
	$(COMPOSE) up -d

# Пересобрать и запустить
up-build:
	$(COMPOSE) up -d --build

# Остановить контейнеры
down:
	$(COMPOSE) down

# Остановить и удалить volumes
clean:
	$(COMPOSE) down -v

# Логи приложения
logs:
	$(COMPOSE) logs -f app

# Логи всех контейнеров
logs-all:
	$(COMPOSE) logs -f

# Пересобрать и перезапустить приложение
restart:
	$(COMPOSE) up -d --build app

# Войти в контейнер приложения
shell:
	$(COMPOSE) exec app sh

# Войти в PostgreSQL
psql:
	$(COMPOSE) exec postgres psql -U postgres -d bank

# Статус
status:
	$(COMPOSE) ps