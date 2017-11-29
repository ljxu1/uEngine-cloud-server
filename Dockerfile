FROM openjdk:8u111-jdk-alpine
VOLUME /tmp
ADD target/uengine-cloud-server-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Xmx256M","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","--spring.profiles.active=docker"]