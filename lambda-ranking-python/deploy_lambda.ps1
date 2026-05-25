# Navigate into your project directory
cd lambda-ranking-python

# Target install libraries into a subfolder named 'package'
pip install --target .\package -r requirements.txt

# Copy your lambda execution script inside the package directory
copy lambda_function.py .\package\

# Navigate into the package directory to compress contents
cd package

# Compress all contents into a deployment ready .zip file on your desktop
Compress-Archive -Path * -DestinationPath "D:\temp\20260518\stock_ranking_lambda.zip" -Force