FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN ./gradlew -p file-storage-service build -x test

EXPOSE 8082

CMD ["./gradlew", "-p", "file-storage-service", "bootRun"]
