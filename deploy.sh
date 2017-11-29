docker login
mvn package -B
docker build -t sppark/uengine-cloud-server:latest ./
docker push sppark/uengine-cloud-server:latest
