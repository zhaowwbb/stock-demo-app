resource "random_string" "suffix" {
  length  = 6
  special = false
  upper   = false
}

resource "aws_s3_bucket" "app_bucket" {
  bucket        = "${var.environment}-data-bucket-${random_string.suffix.result}"
  force_destroy = true
}