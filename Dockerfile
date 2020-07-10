FROM openjdk:11-jre-slim
ENV TZ=Asia/Shanghai
RUN set -eux; \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime; \
    echo $TZ > /etc/timezone
COPY web/target/single-data-service-web-1.0.jar /data/app.jar
CMD ["java","-jar","/data/app.jar","-server"]