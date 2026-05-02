.PHONY: up down logs ps backend-logs frontend-logs build clean restart config

up:
	docker compose up -d --build

down:
	docker compose down

build:
	docker compose build

logs:
	docker compose logs -f --tail=200

ps:
	docker compose ps

backend-logs:
	docker compose logs -f --tail=200 backend

frontend-logs:
	docker compose logs -f --tail=200 frontend

restart:
	docker compose restart

config:
	docker compose config

clean:
	docker compose down -v --remove-orphans
