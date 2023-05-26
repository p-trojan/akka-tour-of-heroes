FROM openjdk:11
WORKDIR /
COPY target/scala-2.13/akka-http-quickstart-sample-assembly-0.1.0-SNAPSHOT.jar home/akka-http-quickstart-sample-assembly-0.1.0-SNAPSHOT.jar
EXPOSE 8080
CMD ["java", "-jar", "home/akka-http-quickstart-sample-assembly-0.1.0-SNAPSHOT.jar"]