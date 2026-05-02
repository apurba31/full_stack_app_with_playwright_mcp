# syntax=docker/dockerfile:1.6

# ---------- Stage 1: Build the Vite app ----------
FROM node:20-alpine AS build

WORKDIR /app

# Copy manifests first for caching. Use a glob so the build doesn't fail if the
# lockfile is missing (we'll fall back to `npm install` in that case).
COPY package.json ./
COPY package-lock.json* ./

RUN if [ -f package-lock.json ]; then \
        npm ci; \
    else \
        npm install; \
    fi

# Copy the rest of the source and build.
COPY . .

RUN npm run build

# ---------- Stage 2: Serve via Nginx ----------
FROM nginx:1.27-alpine AS runtime

# curl for healthcheck.
RUN apk add --no-cache curl

# Replace the default nginx config with our SPA + API-proxy config.
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the built static assets.
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=5 \
    CMD curl -f http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
