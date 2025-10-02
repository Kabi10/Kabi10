# Jaffna Farmers Marketplace Backend - Deployment Guide

This guide provides step-by-step instructions for deploying the Jaffna Farmers Marketplace backend API to production environments optimized for Sri Lankan users.

## 🌍 Hosting Recommendations

### For Sri Lankan Deployment

**1. DigitalOcean (Recommended for MVP)**
- **Region:** Singapore (closest to Sri Lanka)
- **Latency:** ~50-80ms to Colombo
- **Cost:** $12-24/month for small-medium scale
- **Benefits:** Simple setup, good documentation, SSD storage

**2. AWS Asia Pacific (Mumbai)**
- **Region:** ap-south-1
- **Latency:** ~30-50ms to Colombo
- **Cost:** $15-40/month (variable pricing)
- **Benefits:** Enterprise features, auto-scaling, managed services

**3. Local Sri Lankan Providers**
- **Lanka Communication Services (LCS)**
- **Dialog Enterprise**
- **SLT VisionCom**
- **Benefits:** Data sovereignty, local support, potentially lower latency

## 🚀 Production Deployment Options

### Option 1: DigitalOcean Droplet (Recommended for MVP)

#### Step 1: Create Droplet
```bash
# Create Ubuntu 22.04 droplet in Singapore region
# Minimum: 2GB RAM, 1 vCPU, 50GB SSD ($12/month)
# Recommended: 4GB RAM, 2 vCPU, 80GB SSD ($24/month)
```

#### Step 2: Initial Server Setup
```bash
# Connect to droplet
ssh root@your_server_ip

# Update system
apt update && apt upgrade -y

# Install Node.js 18
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
apt-get install -y nodejs

# Install PostgreSQL
apt install postgresql postgresql-contrib -y

# Install PM2 for process management
npm install -g pm2

# Install Nginx
apt install nginx -y

# Install certbot for SSL
apt install certbot python3-certbot-nginx -y
```

#### Step 3: Database Setup
```bash
# Switch to postgres user
sudo -u postgres psql

# Create database and user
CREATE DATABASE jaffna_marketplace;
CREATE USER jaffna_user WITH PASSWORD 'secure_password_here';
GRANT ALL PRIVILEGES ON DATABASE jaffna_marketplace TO jaffna_user;
\q

# Configure PostgreSQL for remote connections (if needed)
sudo nano /etc/postgresql/14/main/postgresql.conf
# Uncomment and set: listen_addresses = '*'

sudo nano /etc/postgresql/14/main/pg_hba.conf
# Add: host all all 0.0.0.0/0 md5

sudo systemctl restart postgresql
```

#### Step 4: Application Deployment
```bash
# Create application user
adduser --system --group jaffna

# Create application directory
mkdir -p /var/www/jaffna-marketplace
chown jaffna:jaffna /var/www/jaffna-marketplace

# Switch to application user
sudo -u jaffna -i

# Clone repository (or upload files)
cd /var/www/jaffna-marketplace
git clone https://github.com/your-repo/jaffna-marketplace-backend.git .

# Install dependencies
npm ci --only=production

# Create environment file
cp .env.example .env
nano .env
```

#### Step 5: Environment Configuration
```env
NODE_ENV=production
PORT=3000
DB_HOST=localhost
DB_PORT=5432
DB_NAME=jaffna_marketplace
DB_USER=jaffna_user
DB_PASSWORD=secure_password_here
DB_SSL=false

JWT_SECRET=your-super-secure-jwt-secret-64-characters-long-random-string
JWT_REFRESH_SECRET=your-super-secure-refresh-secret-64-characters-long-random

# SMS Configuration (Dialog Ideamart)
SMS_PROVIDER=dialog
DIALOG_API_URL=https://api.dialog.lk/sms/send
DIALOG_API_KEY=your_dialog_api_key
DIALOG_API_SECRET=your_dialog_api_secret
DIALOG_SENDER_ID=JaffnaFarm

# Security
BCRYPT_ROUNDS=12
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=100
RATE_LIMIT_OTP_MAX=5

# CORS
CORS_ORIGIN=https://yourdomain.com,https://www.yourdomain.com
```

#### Step 6: Database Migration
```bash
# Run database migration
npm run migrate

# Optionally seed with sample data (development only)
# npm run seed
```

#### Step 7: PM2 Process Management
```bash
# Create PM2 ecosystem file
cat > ecosystem.config.js << EOF
module.exports = {
  apps: [{
    name: 'jaffna-marketplace-api',
    script: 'src/server.js',
    instances: 'max',
    exec_mode: 'cluster',
    env: {
      NODE_ENV: 'production',
      PORT: 3000
    },
    error_file: './logs/err.log',
    out_file: './logs/out.log',
    log_file: './logs/combined.log',
    time: true,
    max_memory_restart: '1G',
    node_args: '--max-old-space-size=1024'
  }]
};
EOF

# Start application with PM2
pm2 start ecosystem.config.js

# Save PM2 configuration
pm2 save

# Setup PM2 startup script
pm2 startup
# Follow the instructions provided by PM2
```

#### Step 8: Nginx Configuration
```bash
# Create Nginx configuration
sudo nano /etc/nginx/sites-available/jaffna-marketplace

# Add configuration:
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        proxy_read_timeout 86400;
    }

    # Health check endpoint
    location /health {
        proxy_pass http://localhost:3000/health;
        access_log off;
    }

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;
}

# Enable site
sudo ln -s /etc/nginx/sites-available/jaffna-marketplace /etc/nginx/sites-enabled/

# Test configuration
sudo nginx -t

# Restart Nginx
sudo systemctl restart nginx
```

#### Step 9: SSL Certificate
```bash
# Obtain SSL certificate
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com

# Test auto-renewal
sudo certbot renew --dry-run
```

### Option 2: Docker Deployment

#### Step 1: Prepare Environment
```bash
# Create production environment file
cp .env.example .env.production

# Edit with production values
nano .env.production
```

#### Step 2: Build and Deploy
```bash
# Build production image
docker build -t jaffna-marketplace-api:latest .

# Run with docker-compose
docker-compose -f docker-compose.yml --env-file .env.production up -d

# Check logs
docker-compose logs -f api
```

### Option 3: AWS ECS Deployment

#### Step 1: Create ECS Cluster
```bash
# Install AWS CLI
aws configure

# Create ECS cluster
aws ecs create-cluster --cluster-name jaffna-marketplace

# Create task definition
aws ecs register-task-definition --cli-input-json file://task-definition.json
```

#### Step 2: Deploy Service
```bash
# Create service
aws ecs create-service \
  --cluster jaffna-marketplace \
  --service-name jaffna-api \
  --task-definition jaffna-marketplace-api:1 \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-12345],securityGroups=[sg-12345],assignPublicIp=ENABLED}"
```

## 📱 SMS Provider Setup

### Dialog Ideamart (Primary)

1. **Register at:** https://www.ideamart.io/
2. **Create SMS API application**
3. **Get API credentials:**
   - API Key
   - API Secret
   - Sender ID approval

```env
SMS_PROVIDER=dialog
DIALOG_API_URL=https://api.dialog.lk/sms/send
DIALOG_API_KEY=your_api_key
DIALOG_API_SECRET=your_api_secret
DIALOG_SENDER_ID=JaffnaFarm
```

### Mobitel mSpace (Alternative)

1. **Register at:** https://mspace.mobitel.lk/
2. **Apply for SMS API access**
3. **Configure credentials:**

```env
SMS_PROVIDER=mobitel
MOBITEL_API_URL=https://api.mobitel.lk/sms
MOBITEL_USERNAME=your_username
MOBITEL_PASSWORD=your_password
```

### Twilio (International Fallback)

1. **Register at:** https://www.twilio.com/
2. **Get account credentials:**

```env
SMS_PROVIDER=twilio
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_PHONE_NUMBER=+1234567890
```

## 🔒 Security Configuration

### Firewall Setup
```bash
# Configure UFW firewall
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow 'Nginx Full'
sudo ufw enable
```

### SSL/TLS Configuration
```bash
# Strong SSL configuration in Nginx
ssl_protocols TLSv1.2 TLSv1.3;
ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
ssl_prefer_server_ciphers off;
ssl_session_cache shared:SSL:10m;
ssl_session_timeout 10m;
```

### Database Security
```bash
# Secure PostgreSQL installation
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'strong_password';"

# Configure pg_hba.conf for security
sudo nano /etc/postgresql/14/main/pg_hba.conf
# Use md5 authentication, restrict access
```

## 📊 Monitoring Setup

### PM2 Monitoring
```bash
# Install PM2 monitoring
pm2 install pm2-server-monit

# View monitoring dashboard
pm2 monit
```

### Log Management
```bash
# Setup log rotation
sudo nano /etc/logrotate.d/jaffna-marketplace

/var/www/jaffna-marketplace/logs/*.log {
    daily
    missingok
    rotate 52
    compress
    delaycompress
    notifempty
    create 644 jaffna jaffna
    postrotate
        pm2 reloadLogs
    endscript
}
```

### Health Monitoring
```bash
# Create health check script
cat > /usr/local/bin/health-check.sh << EOF
#!/bin/bash
response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/health)
if [ $response != "200" ]; then
    echo "Health check failed: $response"
    pm2 restart jaffna-marketplace-api
fi
EOF

chmod +x /usr/local/bin/health-check.sh

# Add to crontab
echo "*/5 * * * * /usr/local/bin/health-check.sh" | crontab -
```

## 💰 Cost Optimization

### Small Scale (100 users)
- **DigitalOcean Droplet (2GB):** $12/month
- **Domain + SSL:** $15/year
- **SMS (1000/month):** $5/month
- **Total:** ~$18/month

### Medium Scale (1000 users)
- **DigitalOcean Droplet (4GB):** $24/month
- **Managed PostgreSQL:** $30/month
- **SMS (10000/month):** $50/month
- **Total:** ~$104/month

### Large Scale (10000+ users)
- **AWS ECS (2 instances):** $60/month
- **RDS PostgreSQL:** $100/month
- **Application Load Balancer:** $20/month
- **SMS (100000/month):** $500/month
- **Total:** ~$680/month

## 🔄 Backup Strategy

### Database Backup
```bash
# Create backup script
cat > /usr/local/bin/backup-db.sh << EOF
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -h localhost -U jaffna_user jaffna_marketplace > /backups/db_backup_$DATE.sql
gzip /backups/db_backup_$DATE.sql

# Keep only last 7 days
find /backups -name "db_backup_*.sql.gz" -mtime +7 -delete
EOF

chmod +x /usr/local/bin/backup-db.sh

# Schedule daily backups
echo "0 2 * * * /usr/local/bin/backup-db.sh" | crontab -
```

### Application Backup
```bash
# Backup application files
tar -czf /backups/app_backup_$(date +%Y%m%d).tar.gz /var/www/jaffna-marketplace
```

## 🚨 Troubleshooting

### Common Issues

**1. Database Connection Failed**
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Check logs
sudo tail -f /var/log/postgresql/postgresql-14-main.log
```

**2. SMS Not Working**
```bash
# Check SMS provider configuration
curl -X POST http://localhost:3000/api/v1/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+94771234567"}'
```

**3. High Memory Usage**
```bash
# Monitor memory
free -h
pm2 monit

# Restart if needed
pm2 restart jaffna-marketplace-api
```

**4. SSL Certificate Issues**
```bash
# Check certificate status
sudo certbot certificates

# Renew if needed
sudo certbot renew
```

## 📞 Support

For deployment support:
- **Email:** devops@jaffna-marketplace.com
- **Documentation:** https://docs.jaffna-marketplace.com
- **Emergency:** +94 77 XXX XXXX

---

**Deployment completed! Your Jaffna Farmers Marketplace backend is now live and ready to serve the farming community of Jaffna, Sri Lanka.**
