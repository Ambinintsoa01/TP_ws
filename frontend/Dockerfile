## =====================================================
## Multi-stage build pour application Vue.js
## Builder: Node.js 22
## Runtime: Nginx Alpine
## =====================================================

FROM node:22-alpine AS build

WORKDIR /app

# Copier les fichiers de configuration
COPY package*.json ./

# Installer les dépendances
RUN npm ci --only=production

# Copier le code source
COPY . .

# Construire l'application
RUN npm run build

# Phase de production
FROM nginx:alpine

# Copier les fichiers buildés vers nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Copier configuration nginx personnalisée si nécessaire
# COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]