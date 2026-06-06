module "vpc" {
  source      = "./modules/vpc"
  environment = var.environment
}

module "s3" {
  source      = "./modules/s3"
  environment = var.environment
}

module "rds" {
  source             = "./modules/rds"
  environment        = var.environment
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  ec2_security_group = module.vpc.ec2_security_group_id
}

module "ec2" {
  source            = "./modules/ec2"
  environment       = var.environment
  vpc_id            = module.vpc.vpc_id
  public_subnet_ids = module.vpc.public_subnet_ids
  security_group_id = module.vpc.ec2_security_group_id
}

module "lambda" {
  source             = "./modules/lambda"
  environment        = var.environment
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
}