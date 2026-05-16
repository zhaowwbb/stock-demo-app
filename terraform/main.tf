resource "aws_s3_bucket" "stock_bucket" {
  bucket = "stock-demo-app-bucket"
}

resource "aws_db_instance" "postgres" {
  identifier = "stock-demo-db"
  engine = "postgres"
  instance_class = "db.t3.micro"
  allocated_storage = 20
  username = "postgres"
  password = "Password123!"
  publicly_accessible = true
  skip_final_snapshot = true
}
