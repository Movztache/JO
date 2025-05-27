# ============================================================================
# TERRAFORM VARIABLES VALUES
# ============================================================================

# General
aws_region   = "eu-west-3"
environment  = "dev"
project_name = "vibe-tickets"

# Network
vpc_cidr           = "10.0.0.0/16"
availability_zones = ["eu-west-3a", "eu-west-3b", "eu-west-3c"]

# EC2
ec2_instance_type = "t3.small"
ec2_key_name      = "vibe-tickets-terraform-key"
my_ip             = "109.209.31.42"  # Votre IP publique

# RDS
db_instance_class    = "db.t3.micro"
db_allocated_storage = 20
db_name              = "jeux_olympiques"
db_username          = "adminco"
db_password          = "adminco123"

# ECR
ecr_repository_name = "vibe-tickets"

# Frontend
frontend_url = "https://d2rp6qs91n8yy7.cloudfront.net"
