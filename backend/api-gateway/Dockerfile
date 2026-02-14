FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

# Copiar parent POM e instalar (non-recursive, no necesita modulos)
COPY pom.xml ./
RUN mvn install -N -q -B

# Copiar y compilar shared-kernel
COPY shared-kernel/ shared-kernel/
RUN mvn install -f shared-kernel/pom.xml -DskipTests -q -B

# Copiar y compilar el modulo
COPY api-gateway/ api-gateway/
RUN mvn package -f api-gateway/pom.xml -DskipTests -q -B

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /workspace/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
