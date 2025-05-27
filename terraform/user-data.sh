#!/bin/bash

# ============================================================================
# VIBE TICKETS - EC2 USER DATA SCRIPT
# ============================================================================
#
# Ce script s'exécute automatiquement au démarrage de l'instance EC2 :
# 1. Met à jour le système et installe Docker, AWS CLI, PostgreSQL client
# 2. Configure l'accès ECR via le rôle IAM (sécurisé)
# 3. Télécharge et démarre l'image Docker de l'application
# 4. Crée la base de données si elle n'existe pas
# 5. Configure les health checks et monitoring
#
# Toutes les opérations sont logées dans /var/log/user-data.log
# ============================================================================

# Logging setup
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1
echo "=== STARTING EC2 CONFIGURATION - $(date) ==="
echo "Deployment timestamp: $(date +%s)"
echo "Deployment hash: ${deployment_hash}"

# ============================================================================
# SYSTEM UPDATE
# ============================================================================

echo "Updating system packages..."
yum update -y

# ============================================================================
# DOCKER INSTALLATION
# ============================================================================

echo "Installing Docker..."
yum install -y docker
systemctl start docker
systemctl enable docker
usermod -a -G docker ec2-user

# ============================================================================
# AWS CLI INSTALLATION
# ============================================================================

echo "Installing AWS CLI v2..."
yum install -y unzip
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# ============================================================================
# POSTGRESQL CLIENT INSTALLATION
# ============================================================================

echo "Installing PostgreSQL client..."
yum install -y postgresql15

# ============================================================================
# AWS CONFIGURATION (Using IAM Role - No Credentials Needed)
# ============================================================================

echo "Configuring AWS CLI to use IAM role..."
# IAM role provides automatic credentials via instance metadata
# No manual credentials configuration needed

echo "Waiting for IAM role to be available..."
sleep 30

echo "Testing IAM role access..."
aws sts get-caller-identity
if [ $? -ne 0 ]; then
    echo "ERROR: IAM role not available, waiting longer..."
    sleep 60
    aws sts get-caller-identity
fi

# ============================================================================
# ECR LOGIN AND IMAGE PULL
# ============================================================================

echo "Logging into ECR..."
aws ecr get-login-password --region ${aws_region} | docker login --username AWS --password-stdin ${ecr_repository_uri}

echo "Pulling application image..."
docker pull ${ecr_repository_uri}:latest

# ============================================================================
# DATABASE SETUP
# ============================================================================

echo "Waiting for RDS to be available..."
sleep 60

echo "Creating database if not exists..."
PGPASSWORD=${db_password} psql -h ${db_endpoint} -U ${db_username} -d postgres -c "SELECT 1 FROM pg_database WHERE datname = '${db_name}'" | grep -q 1 || PGPASSWORD=${db_password} psql -h ${db_endpoint} -U ${db_username} -d postgres -c "CREATE DATABASE ${db_name};"

# ============================================================================
# APPLICATION DEPLOYMENT
# ============================================================================

echo "Starting application container..."
docker run -d \
  --name vibe-tickets \
  --restart unless-stopped \
  -p 8080:8080 \
  -e DATABASE_URL="jdbc:postgresql://${db_endpoint}:5432/${db_name}" \
  -e DATABASE_USERNAME="${db_username}" \
  -e DATABASE_PASSWORD="${db_password}" \
  -e SPRING_PROFILES_ACTIVE="docker" \
  -e CORS_ORIGINS="http://localhost:4200,https://d2rp6qs91n8yy7.cloudfront.net" \
  ${ecr_repository_uri}:latest

# ============================================================================
# HEALTH CHECK SETUP
# ============================================================================

echo "Setting up health check script..."
cat > /home/ec2-user/health-check.sh << 'EOF'
#!/bin/bash
curl -f http://localhost:8080/actuator/health || exit 1
EOF

chmod +x /home/ec2-user/health-check.sh
chown ec2-user:ec2-user /home/ec2-user/health-check.sh

# ============================================================================
# COMPLETION
# ============================================================================

echo "=== EC2 CONFIGURATION COMPLETED - $(date) ==="
echo "Application should be available at: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8080"
echo "Check application status: docker ps"
echo "Check application logs: docker logs vibe-tickets"
