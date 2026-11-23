# 构建阶段
FROM maven:3.8.6-jdk-8 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:8-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/target/lingxi-backend.jar app.jar
RUN groupadd --system app \
    && useradd --system --gid app app \
    && mkdir -p /app/logs \
    && chown -R app:app /app
USER app
EXPOSE 8080
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
