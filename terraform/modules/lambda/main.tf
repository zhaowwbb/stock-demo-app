resource "aws_security_group" "lambda_sg" {
  name   = "${var.environment}-lambda-sg"
  vpc_id = var.vpc_id
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_iam_role" "lambda_role" {
  name = "${var.environment}-lambda-execution-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_vpc" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

# Dummy deployment packages to let Terraform apply safely without missing local binaries
data "archive_file" "dummy" {
  type        = "zip"
  output_path = "${path.module}/dummy.zip"
  source {
    content  = "placeholder"
    filename = "placeholder.txt"
  }
}

resource "aws_lambda_function" "go_func" {
  filename         = data.archive_file.dummy.output_path
  function_name    = "${var.environment}-go-service"
  role             = aws_iam_role.lambda_role.arn
  handler          = "bootstrap"
  runtime          = "provided.al2023"
  vpc_config {
    subnet_ids         = var.private_subnet_ids
    security_group_ids = [aws_security_group.lambda_sg.id]
  }
}

resource "aws_lambda_function" "python_func" {
  filename         = data.archive_file.dummy.output_path
  function_name    = "${var.environment}-python-service"
  role             = aws_iam_role.lambda_role.arn
  handler          = "index.handler"
  runtime          = "python3.11"
  vpc_config {
    subnet_ids         = var.private_subnet_ids
    security_group_ids = [aws_security_group.lambda_sg.id]
  }
}