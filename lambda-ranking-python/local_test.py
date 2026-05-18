import os

# 1. Mock the AWS Environment Variables with your actual RDS credentials
# (For safety, ensure your RDS instance allows your home IP address in its Security Group)
os.environ["DB_HOST"] = "localhost"
os.environ["DB_NAME"] = "zafin"
os.environ["DB_USER"] = "dm"
os.environ["DB_PASSWORD"] = "dm"

# 2. Import your lambda function
import lambda_function

# 3. Simulate an empty AWS Event and Context
mock_event = {}
mock_context = None

# 4. Execute the handler function manually
print("--- Starting Local Lambda Test ---")
response = lambda_function.lambda_handler(mock_event, mock_context)
print("--- Test Complete ---")
print(f"Response Received: {response}")