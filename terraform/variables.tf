# ============================================================================
# GENERAL VARIABLES
# ============================================================================

variable "aws_region" {
  description = "AWS region for resources"
  type        = string
  default     = "eu-west-3"
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "project_name" {
  description = "Name of the project"
  type        = string
  default     = "vibe-tickets"
}

# ============================================================================
# NETWORK VARIABLES
# ============================================================================

variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "Availability zones"
  type        = list(string)
  default     = ["eu-west-3a", "eu-west-3b", "eu-west-3c"]
}

# ============================================================================
# EC2 VARIABLES
# ============================================================================

variable "ec2_instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.small"
}

variable "ec2_key_name" {
  description = "EC2 Key Pair name"
  type        = string
  default     = "vibe-tickets-terraform-key"
}

variable "my_ip" {
  description = "Your public IP for SSH access"
  type        = string
  # Cette valeur sera définie dans terraform.tfvars
}

# ============================================================================
# RDS VARIABLES
# ============================================================================

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  description = "RDS allocated storage in GB"
  type        = number
  default     = 20
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "vibe_tickets"
}

variable "db_username" {
  description = "Database master username"
  type        = string
  default     = "adminco"
}

variable "db_password" {
  description = "Database master password"
  type        = string
  sensitive   = true
  default     = "adminco123"
}

# ============================================================================
# ECR VARIABLES
# ============================================================================

variable "ecr_repository_name" {
  description = "ECR repository name"
  type        = string
  default     = "vibe-tickets"
}

variable "image_tag" {
  description = "Docker image tag to deploy (format: v20241202-143055)"
  type        = string
  default     = "latest"  # Sera remplacé par le script de déploiement avec timestamp
}

# ============================================================================
# FRONTEND VARIABLES
# ============================================================================

variable "frontend_url" {
  description = "URL of the frontend application (CloudFront HTTP)"
  type        = string
  default     = "http://d3o32gj1vfio9o.cloudfront.net"
}
