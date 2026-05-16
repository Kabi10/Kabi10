FROM python:3.12-slim

WORKDIR /app

# Install dependencies first (layer caching)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy source
COPY src/ ./src/
COPY data/ ./data/
COPY .env.example .env

# Workspace dir for file tool
RUN mkdir -p workspace

# SSE transport for containerized deployment
ENV TRANSPORT=sse
ENV AUTH_ENABLED=true

EXPOSE 8000

CMD ["python", "-m", "src.server"]
