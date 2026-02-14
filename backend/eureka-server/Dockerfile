FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

# Copiar parent POM e instalar (non-recursive, no necesita modulos)
COPY pom.xml ./
RUN mvn install -N -q -B

# Copiar y compilar shared-kernel
COPY shared-kernel/ shared-kernel/
RUN mvn install -f shared-kernel/pom.xml -DskipTests -q -B

# Copiar y compilar el modulo
COPY eureka-server/ eureka-server/
RUN mvn package -DskipTests -q -B -f eureka-server/pom.xml

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /workspace/eureka-server/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
