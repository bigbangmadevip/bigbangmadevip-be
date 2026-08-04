FROM eclipse-temurin:17-jre
ARG JAR_FILE=build/libs/bigbangmadevip-be.jar
COPY ${JAR_FILE} bigbangmadevip-be.jar
ENTRYPOINT ["java", "-jar", "/bigbangmadevip-be.jar"]
