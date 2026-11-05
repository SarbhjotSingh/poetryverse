# ============================================================================
# STAGE 1: BUILD POETRYVERSE SPRING BOOT JAR WITH MAVEN & JDK 17
# ============================================================================
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first for dependency layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy project source and package executable JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================================
# STAGE 2: PRODUCTION RUNTIME IMAGE (ECLIPSE TEMURIN JRE 17)
# ============================================================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/poetryverse-1.0.0.jar app.jar
COPY database ./database

EXPOSE 8080

ENV PORT=8080
ENV JAVA_OPTS="-Xmx384m -Xms128m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
