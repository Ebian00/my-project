
FROM java:8-jdk-alpine
COPY  target/caas-0.0.1-SNAPSHOT.jar /usr/app/
WORKDIR /usr/app
EXPOSE 8080
ENTRYPOINT ["java","-jar","caas-0.0.1-SNAPSHOT.jar"]