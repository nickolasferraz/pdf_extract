# ============================================
# Stage 1: Build (usa JDK completo para compilar)
# ============================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copia o Maven Wrapper e pom.xml primeiro
# Isso permite que o Docker cache as dependências
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Baixa todas as dependências (camada cacheada se pom.xml não mudar)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Agora copia o código fonte
COPY src/ src/

# Gera o JAR (sem rodar testes — já foram rodados no CI)
RUN ./mvnw clean package -DskipTests -B

# ============================================
# Stage 2: Runtime (imagem leve, só JRE)
# ============================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Cria diretório para uploads
RUN mkdir -p /app/uploads

# Copia apenas o JAR do stage de build
COPY --from=builder /app/target/*.jar app.jar

# Porta da aplicação Spring Boot
EXPOSE 8080

# Executa a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
