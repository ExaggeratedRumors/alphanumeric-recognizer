FROM openjdk:20-jdk-slim AS build

RUN apt-get update \
	&& apt-get install --no-install-recommends -y \
	libfreetype6 \
	libfreetype6-dev \
	fontconfig \
	bash \
	&& apt-get clean \
	&& rm -rf /var/cache/* /var/lib/apt/lists/* /tmp/*

WORKDIR /app

RUN --mount=type=bind,source=/build/libs,target=/temp/libs \
	bash -o pipefail \
	-c "newest_release=\$(ls -v /temp/libs/alphanumeric-recognizer-*.jar | tail -n 1) && cp \"\$newest_release\" /app/app.jar"

FROM openjdk:20-jdk-slim AS runtime

RUN apt-get update \
	&& apt-get install --no-install-recommends -y \
	libfreetype6 \
	libfreetype6-dev \
	fontconfig \
	bash \
	&& apt-get clean \
	&& rm -rf /var/cache/* /var/lib/apt/lists/* /tmp/*

WORKDIR /app

COPY --from=build /app/app.jar /app/app.jar
RUN mkdir -p /models /data
ENTRYPOINT ["java","-jar","/app/app.jar"]
