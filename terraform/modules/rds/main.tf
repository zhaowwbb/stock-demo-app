resource "aws_db_subnet_group" "rds" {
  name       = "${var.environment}-rds-subnet-group"
  subnet_ids = var.private_subnet_ids
}

resource "aws_security_group" "rds_sg" {
  name   = "${var.environment}-rds-sg"
  vpc_id = var.vpc_id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [var.ec2_security_group]
  }
}

resource "aws_db_instance" "postgres" {
  identifier             = "${var.environment}-postgres"
  allocated_storage      = 20               # 20 GB is free-tier eligible (up to 20GB allocated)
  engine                 = "postgres"
  engine_version         = "15"
  instance_class         = "db.t3.micro"    # CHANGED: db.t4g.micro replaced with Free-Tier eligible t3.micro
  db_name                = "appdb"
  username               = "cloud_admin"
  password               = "ManagedByTerraformSecret123!" 
  db_subnet_group_name   = aws_db_subnet_group.rds.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  skip_final_snapshot    = true
}