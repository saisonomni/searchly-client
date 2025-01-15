export CODEARTIFACT_AUTH_TOKEN=`aws codeartifact get-authorization-token --domain saisonomni --domain-owner 958072769333 --region us-east-1 --query authorizationToken --output text`
echo "codeartifactToken=$CODEARTIFACT_AUTH_TOKEN" > ~/.gradle/gradle.properties
echo "artifactoryUrl=https://saisonomni-958072769333.d.codeartifact.us-east-1.amazonaws.com/maven/common/" >> ~/.gradle/gradle.properties