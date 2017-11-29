docker login
mvn package -B
docker build -t sppark/uengine-cloud-ui:latest ./
docker push sppark/uengine-cloud-ui:latest
