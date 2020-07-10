FROM openjdk:11-jre-slim
COPY web/target/single-data-service-web-1.0.jar /data/app.jar
ENV TZ=Asia/Shanghai
RUN set -eux; \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime; \
    echo $TZ > /etc/timezone
CMD ["java","-jar","/data/app.jar","-server"]