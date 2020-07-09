FROM openjdk:11-jre-slim
COPY web/target/single-data-service-web-1.0.jar /data/app.jar
CMD ["java","-jar","/data/app.jar","-server"]