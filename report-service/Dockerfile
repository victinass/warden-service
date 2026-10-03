FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN ./gradlew -p report-service build -x test

EXPOSE 8081

CMD ["./gradlew", "-p", "report-service", "bootRun"]