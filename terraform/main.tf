# ============================================================================
# INFRASTRUCTURE TERRAFORM - APPLICATION VIBE-TICKETS
# ============================================================================
#
# Ce fichier définit l'infrastructure complète AWS pour l'application Vibe-Tickets :
#
# ARCHITECTURE :
# ┌─────────────────────────────────────────────────────────────────────────┐
# │                            INFRASTRUCTURE AWS                           │
# ├─────────────────────────────────────────────────────────────────────────┤
# │ VPC (10.0.0.0/16)                                                      │
# │ ├── Subnets Publics (Multi-AZ)  : Instance EC2 + Load Balancer         │
# │ ├── Subnets Privés (Multi-AZ)   : Base de données RDS                  │
# │ ├── Internet Gateway            : Accès Internet                       │
# │ └── NAT Gateway                 : Accès sortant pour subnets privés    │
# │                                                                         │
# │ SERVICES :                                                              │
# │ ├── EC2 Instance (Amazon Linux) : Application Spring Boot + Docker     │
# │ ├── RDS PostgreSQL              : Base de données                      │
# │ ├── ECR Repository              : Images Docker                        │
# │ ├── Elastic IP                  : IP publique stable                   │
# │ └── IAM Roles                   : Permissions sécurisées               │
# │                                                                         │
# │ SÉCURITÉ :                                                              │
# │ ├── Security Groups             : Contrôle d'accès réseau              │
# │ ├── Clés SSH dynamiques         : Générées à chaque déploiement        │
# │ ├── Chiffrement                 : EBS + RDS chiffrés                   │
# │ └── Principe du moindre privilège : IAM policies restrictives          │
# └─────────────────────────────────────────────────────────────────────────┘
#
# PRÉREQUIS :
# - AWS CLI configuré avec les bonnes permissions
# - Fichier terraform.tfvars avec les variables personnalisées
# - Clés SSH générées par le script deploy.ps1
#
# DÉPLOIEMENT :
# - Utiliser le script deploy.ps1 pour un déploiement automatisé complet
# - Ne pas exécuter terraform directement (gestion des clés SSH requise)
#
# ============================================================================

# ============================================================================
# DATA SOURCES - RÉCUPÉRATION D'INFORMATIONS AWS
# ============================================================================

# Récupération de la dernière AMI Amazon Linux 2023
# Cette AMI est utilisée pour l'instance EC2 qui hébergera l'application
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  # Filtrer pour obtenir Amazon Linux 2023
  filter {
    name   = "name"
    values = ["al2023-ami-*"]
  }

  # Architecture x86_64 (compatible avec la plupart des instances)
  filter {
    name   = "architecture"
    values = ["x86_64"]
  }

  # Seulement les AMIs disponibles
  filter {
    name   = "state"
    values = ["available"]
  }
}

# Récupération des zones de disponibilité de la région
data "aws_availability_zones" "available" {
  state = "available"
}

# Get current AWS account ID
data "aws_caller_identity" "current" {}

# ============================================================================
# VPC AND NETWORKING
# ============================================================================

resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "${var.project_name}-vpc"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "${var.project_name}-igw"
  }
}

# Public Subnets
resource "aws_subnet" "public" {
  count = length(var.availability_zones)

  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.${count.index + 1}.0/24"
  availability_zone       = var.availability_zones[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.project_name}-public-subnet-${count.index + 1}"
    Type = "Public"
  }
}

# Private Subnets for RDS
resource "aws_subnet" "private" {
  count = length(var.availability_zones)

  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index + 10}.0/24"
  availability_zone = var.availability_zones[count.index]

  tags = {
    Name = "${var.project_name}-private-subnet-${count.index + 1}"
    Type = "Private"
  }
}

# Route Table for Public Subnets
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "${var.project_name}-public-rt"
  }
}

# Associate Public Subnets with Route Table
resource "aws_route_table_association" "public" {
  count = length(aws_subnet.public)

  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

# ============================================================================
# SECURITY GROUPS
# ============================================================================

# Security Group for EC2
resource "aws_security_group" "ec2" {
  name_prefix = "${var.project_name}-ec2-"
  vpc_id      = aws_vpc.main.id

  # SSH access from your IP
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["${var.my_ip}/32"]
    description = "SSH access from my IP"
  }

  # HTTP access for application
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTP access for Spring Boot app"
  }

  # All outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "All outbound traffic"
  }

  tags = {
    Name = "${var.project_name}-ec2-sg"
  }
}

# Security Group for RDS
resource "aws_security_group" "rds" {
  name_prefix = "${var.project_name}-rds-"
  vpc_id      = aws_vpc.main.id

  # PostgreSQL access from EC2
  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2.id]
    description     = "PostgreSQL access from EC2"
  }

  # PostgreSQL access from your IP (for testing)
  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = ["${var.my_ip}/32"]
    description = "PostgreSQL access from my IP for testing"
  }

  # All outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "All outbound traffic"
  }

  tags = {
    Name = "${var.project_name}-rds-sg"
  }
}

# ============================================================================
# CLÉS SSH POUR L'ACCÈS EC2
# ============================================================================
#
# SÉCURITÉ DES CLÉS SSH :
# - Les clés SSH sont générées automatiquement par le script deploy.ps1
# - Chaque déploiement utilise une nouvelle paire de clés avec timestamp
# - Les anciennes clés sont automatiquement supprimées
# - La clé privée n'est jamais commitée dans Git
#
# PROCESSUS :
# 1. deploy.ps1 génère ssh-key et ssh-key.pub dans terraform/
# 2. Terraform utilise ssh-key.pub pour créer la key pair AWS
# 3. L'instance EC2 est configurée avec cette clé
# 4. Connexion SSH possible avec : ssh -i terraform/ssh-key ec2-user@<IP>
#
# IMPORTANT :
# - Ne jamais exécuter terraform directement sans passer par deploy.ps1
# - Les fichiers ssh-key* sont dans .gitignore pour la sécurité
# ============================================================================

resource "aws_key_pair" "main" {
  key_name   = var.ec2_key_name
  public_key = file("${path.module}/ssh-key.pub")

  tags = {
    Name        = "${var.project_name}-key-pair"
    Description = "Clé SSH générée automatiquement pour ${var.ec2_key_name}"
    CreatedBy   = "deploy.ps1"
  }

  # Lifecycle pour éviter les conflits lors des redéploiements
  lifecycle {
    create_before_destroy = true
  }
}

# ============================================================================
# ECR REPOSITORY
# ============================================================================

resource "aws_ecr_repository" "main" {
  name                 = var.ecr_repository_name
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "${var.project_name}-ecr"
  }
}

# ============================================================================
# RDS SUBNET GROUP
# ============================================================================

resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = aws_subnet.private[*].id

  tags = {
    Name = "${var.project_name}-db-subnet-group"
  }
}

# ============================================================================
# RDS INSTANCE
# ============================================================================

resource "aws_db_instance" "main" {
  identifier = "${var.project_name}-db"

  # Engine configuration
  engine         = "postgres"
  engine_version = "15.12"  # Version actuelle de la base existante
  instance_class = var.db_instance_class

  # Storage configuration
  allocated_storage     = var.db_allocated_storage
  max_allocated_storage = 100
  storage_type          = "gp2"
  storage_encrypted     = true

  # Database configuration
  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  # Network configuration
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = true  # Accessible publiquement pour les tests

  # Backup configuration
  backup_retention_period = 7
  backup_window          = "03:00-04:00"
  maintenance_window     = "sun:04:00-sun:05:00"

  # Monitoring
  monitoring_interval = 0

  # Deletion protection
  deletion_protection = false
  skip_final_snapshot = true

  tags = {
    Name = "${var.project_name}-database"
  }
}

# ============================================================================
# IAM ROLES FOR EC2 (Commented until permissions are granted)
# ============================================================================

resource "aws_iam_role" "ec2" {
  name = "${var.project_name}-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "${var.project_name}-ec2-role"
  }
}

# IAM Policy for ECR access
resource "aws_iam_role_policy" "ec2_ecr" {
  name = "${var.project_name}-ec2-ecr-policy"
  role = aws_iam_role.ec2.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage"
        ]
        Resource = "*"
      }
    ]
  })
}

# Instance Profile for EC2
resource "aws_iam_instance_profile" "ec2" {
  name = "${var.project_name}-ec2-profile"
  role = aws_iam_role.ec2.name

  tags = {
    Name = "${var.project_name}-ec2-profile"
  }
}

# ============================================================================
# USER DATA SCRIPT
# ============================================================================

locals {
  # Unique hash to force user-data update on every apply
  deployment_hash = sha256("${timestamp()}-${filesha256("${path.module}/../scripts/deployment/user-data.sh")}")

  user_data = base64encode(templatefile("${path.module}/../scripts/deployment/user-data.sh", {
    ecr_repository_uri = aws_ecr_repository.main.repository_url
    db_endpoint        = split(":", aws_db_instance.main.endpoint)[0]
    db_name            = var.db_name
    db_username        = var.db_username
    db_password        = var.db_password
    aws_region         = var.aws_region
    deployment_hash    = local.deployment_hash
    frontend_url       = var.frontend_url
    image_tag          = var.image_tag
  }))
}

# ============================================================================
# ELASTIC IP FOR STABLE IP ADDRESS
# ============================================================================

resource "aws_eip" "main" {
  domain = "vpc"

  tags = {
    Name = "${var.project_name}-eip"
  }

  # Ensure the internet gateway exists before creating EIP
  depends_on = [aws_internet_gateway.main]
}

# Associate Elastic IP with EC2 instance
resource "aws_eip_association" "main" {
  instance_id   = aws_instance.main.id
  allocation_id = aws_eip.main.id
}

# ============================================================================
# EC2 INSTANCE
# ============================================================================

resource "aws_instance" "main" {
  ami           = data.aws_ami.amazon_linux.id
  instance_type = var.ec2_instance_type
  key_name      = aws_key_pair.main.key_name

  # Network configuration
  subnet_id                   = aws_subnet.public[0].id
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  associate_public_ip_address = true

  # IAM configuration
  iam_instance_profile = aws_iam_instance_profile.ec2.name

  # User data script
  user_data                   = local.user_data
  user_data_replace_on_change = true

  # Storage
  root_block_device {
    volume_type = "gp3"
    volume_size = 30
    encrypted   = true
  }

  tags = {
    Name = "${var.project_name}-server"
  }

  # Ensure RDS is created before EC2
  depends_on = [aws_db_instance.main]
}
