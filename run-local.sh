set -echo
profile="aws-services"
lumeris-aws login -p $profile 
docker build -t 860513963338.dkr.ecr.us-west-2.amazonaws.com/hygieia-deploy-bamboo-collector:latest .

echo "Logging into AWS ECR"
# aws-cli version1
DOCKER_LOGIN_COMMAND=$(aws ecr get-login --region us-west-2 --no-include-email --profile $profile)
DOCKER_LOGIN_RESPONSE=`$DOCKER_LOGIN_COMMAND`
echo "$DOCKER_LOGIN_RESPONSE"
# aws-cli version2
# PASSWORD=$(aws ecr get-login-password --region us-east-1 --profile $profile)
# docker login --username AWS --password $PASSWORD
docker push 860513963338.dkr.ecr.us-west-2.amazonaws.com/hygieia-deploy-bamboo-collector:latest
