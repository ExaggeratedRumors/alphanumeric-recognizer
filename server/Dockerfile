FROM openjdk:20-jdk-slim as build

RUN apt-get update && apt-get install -y \
    libfreetype6 \
    libfreetype6-dev \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY build/libs/ /temp/libs/
RUN newest_release=$(ls -v /temp/libs/alphanumeric-recognizer-*.jar | tail -n 1) && cp "$newest_release" /app/app.jar
RUN rm -rf /temp/libs

FROM openjdk:20-jdk-slim as runtime

RUN apt-get update && apt-get install -y \
    libfreetype6 \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/app.jar /app/app.jar
RUN mkdir -p /models
RUN mkdir -p /data
ENTRYPOINT ["java","-jar","/app/app.jar"]
