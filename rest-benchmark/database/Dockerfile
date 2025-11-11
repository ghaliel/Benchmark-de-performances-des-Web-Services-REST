FROM postgres:14

# Copier les scripts d'initialisation
COPY init-scripts/ /docker-entrypoint-initdb.d/

# Donner les permissions d'exécution
RUN chmod +x /docker-entrypoint-initdb.d/*.sql

# Health check
HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
  CMD pg_isready -U benchmark_user -d benchmark_db

EXPOSE 5432