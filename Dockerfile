FROM maven:3.5-jdk-8-alpine as builder
#打包maven3.5和jdk8的镜像
#form是docker镜像的构建阶段，使用maven:3.5-jdk-8-alpine作为基础镜像，并命名为builder
# Copy local code to the container image.
WORKDIR /app
#指定镜像的工作目录为/app
COPY pom.xml .
#复制pom.xml到镜像的/app目录下
COPY src ./src
#复制src目录到镜像的/app目录下

# Build a release artifact.
RUN mvn package -DskipTests
#使用maven命令打包项目，跳过测试用例
# Run the web service on container startup.
CMD ["java","-jar","/app/target/user_center-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]
#启动容器时自动执行的命令(相当于initalstat)，启动java -jar命令，并指定spring.profiles.active=prod参数，以使用prod环境配置