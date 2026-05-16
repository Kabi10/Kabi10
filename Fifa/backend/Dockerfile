FROM python:3.12-slim

WORKDIR /app

# Install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy source
COPY . .

# Render.com expects the process to write to stdout
ENV PYTHONUNBUFFERED=1

CMD ["python", "poller.py"]
