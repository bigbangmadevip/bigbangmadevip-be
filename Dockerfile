FROM eclipse-temurin:17-jre
ENV TZ=Asia/Seoul
ARG JAR_FILE=build/libs/bigbangmadevip-be.jar
COPY ${JAR_FILE} bigbangmadevip-be.jar
ENTRYPOINT ["java", "-jar", "/bigbangmadevip-be.jar"]
