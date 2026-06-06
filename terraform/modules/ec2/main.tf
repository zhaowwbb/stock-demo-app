data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]
  filter {
    name   = "name"
    values = ["al2023-ami-2023*x86_64"]
  }
}

resource "aws_instance" "spring_boot" {
  ami                    = data.aws_ami.amazon_linux_2023.id
  instance_type          = "t3.micro"       # Safe fallback for standard Free Tier across almost all regions
  subnet_id              = var.public_subnet_ids[0]
  vpc_security_group_ids = [var.security_group_id]

  user_data = <<-EOF
              #!/bin/bash
              sudo dnf update -y
              sudo dnf install java-17-amazon-corretto -y
              EOF

  tags = { Name = "${var.environment}-springboot-api" }
}