FROM openjdk:20-jdk-slim

RUN apt-get update && apt-get install -y \
    libfreetype6 \
    libfreetype6-dev \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir /app
COPY ./build/libs/alphanumeric-recognizer-1.0.jar /app/app.jar
RUN mkdir -p /models
RUN mkdir -p /data
ENTRYPOINT ["java","-jar","/app/app.jar"]
