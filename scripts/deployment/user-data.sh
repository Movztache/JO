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
# DATABASE SEEDING (AVANT DÉMARRAGE APPLICATION)
# ============================================================================

echo "Populating database with initial data BEFORE starting application..."
cat > /tmp/populate_database.sql << 'EOF'
-- ============================================================================
-- SCRIPT DE REMPLISSAGE DE LA BASE DE DONNÉES VIBE-TICKETS
-- ============================================================================

-- Insertion des rôles
INSERT INTO role (name) VALUES
('User'),
('Admin')
ON CONFLICT (name) DO NOTHING;

-- Insertion des utilisateurs (1 Admin + 2 Users)
-- CHAQUE UTILISATEUR A UN MOT DE PASSE DIFFÉRENT
-- Hashes BCrypt valides de 60 caractères exactement
INSERT INTO user_app (first_name, last_name, email, password, user_key, role_id) VALUES
('Simon', 'Admin', 'admin@vibe-tickets.com', '$2a$10$8X9QVqjFGH2kL5mN7pR3sOuT6wE8rY4iU1oP9aS2dF5gH7jK3lM6n', 'ADMIN001', 2),
('Alice', 'Martin', 'alice.martin@email.com', '$2a$10$3F7gH9jK2lM5nP8qR1sT4uV6wX0yZ2aB4cD6eF8gH0iJ2kL4mN6oP', 'USER001', 1),
('Bob', 'Dupont', 'bob.dupont@email.com', '$2a$10$5G9iJ1kL3mN7oP0qR3sT6uV8wX2yZ4aB6cD8eF0gH2iJ4kL6mN8oP', 'USER002', 1)
ON CONFLICT (email) DO NOTHING;

-- Insertion des offres (12 Disponibles + 3 Indisponibles pour tests)
INSERT INTO offer (name, description, price, person_count, offer_type, available) VALUES
-- OFFRES DISPONIBLES
('Concert Rock Festival', 'Concert de rock avec les meilleurs groupes européens', 89.99, 1, 'Concert', true),
('Match Football PSG vs OM', 'Classique français au Parc des Princes', 125.50, 1, 'Sport', true),
('Théâtre - Le Roi Lion', 'Spectacle musical familial', 65.00, 1, 'Théâtre', true),
('Festival Jazz Montreux', 'Festival de jazz international', 95.00, 1, 'Festival', true),
('Opéra - La Traviata', 'Opéra classique de Verdi', 110.00, 1, 'Opéra', true),
('Concert Électro Techno', 'Soirée électro avec DJs internationaux', 45.00, 1, 'Concert', true),
('Comédie Musicale Hamilton', 'Spectacle Broadway à Londres', 150.00, 1, 'Théâtre', true),
('Match Tennis Roland Garros', 'Finale homme Roland Garros', 200.00, 1, 'Sport', true),
('Concert Pop Taylor Swift', 'Concert exceptionnel de la star mondiale', 175.00, 1, 'Concert', true),
('Match Rugby France vs Angleterre', 'Tournoi des 6 Nations au Stade de France', 85.00, 1, 'Sport', true),
('Festival Cannes Film', 'Projection exclusive et tapis rouge', 300.00, 1, 'Festival', true),
('Cirque du Soleil', 'Spectacle acrobatique époustouflant', 95.00, 1, 'Spectacle', true),
-- OFFRES INDISPONIBLES (pour tester les filtres)
('Concert Classique Philharmonie', 'Orchestre de Paris - Beethoven', 75.00, 1, 'Concert', false),
('Festival Électro Tomorrowland', 'Le plus grand festival électro', 180.00, 1, 'Festival', false),
('Match Basket NBA Paris', 'Match exceptionnel NBA à Paris', 250.00, 1, 'Sport', false)
ON CONFLICT (name) DO NOTHING;
EOF

# Exécuter le script de population
PGPASSWORD=${db_password} psql -h ${db_endpoint} -U ${db_username} -d ${db_name} -f /tmp/populate_database.sql

echo "Database seeding completed."

# ============================================================================
# APPLICATION DEPLOYMENT (APRÈS SEEDING)
# ============================================================================

echo "Starting application container with pre-populated database..."
docker run -d \
  --name vibe-tickets \
  --restart unless-stopped \
  -p 8080:8080 \
  -e DATABASE_URL="jdbc:postgresql://${db_endpoint}:5432/${db_name}" \
  -e DATABASE_USERNAME="${db_username}" \
  -e DATABASE_PASSWORD="${db_password}" \
  -e SPRING_PROFILES_ACTIVE="docker" \
  -e CORS_ORIGINS="http://localhost:4200,${frontend_url},http://vibe-ticket-frontend-prod-6ju5v907.s3-website.eu-west-3.amazonaws.com" \
  -e JWT_SECRET="aJmA6#S6f!K5rbNRqBaRJsMrd&K6adeyNPpFM4Qr" \
  -e JWT_EXPIRATION="86400000" \
  ${ecr_repository_uri}:${image_tag}

echo "Application started. Waiting for startup..."
sleep 30

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
