FROM fnproject/fn-java-fdk-build:jdk17-1.0.175 as build-stage
WORKDIR /function
ENV MAVEN_OPTS -Dhttp.proxyHost= -Dhttp.proxyPort= -Dhttps.proxyHost= -Dhttps.proxyPort= -Dhttp.nonProxyHosts= -Dmaven.repo.local=/usr/share/maven/ref/repository
ADD pom.xml /function/pom.xml
ADD opentelemetry-javaagent.jar /function/opentelemetry-javaagent.jar
RUN ["mvn", "package", "dependency:copy-dependencies", "-DincludeScope=runtime", "-DskipTests=true", "-Dmdep.prependGroupId=true", "-DoutputDirectory=target", "--fail-never"]
ADD src /function/src
RUN ["mvn", "package"]
FROM fnproject/fn-java-fdk:jre17-1.0.175
WORKDIR /function
COPY --from=build-stage /function/target/*.jar /function/app/
COPY --from=build-stage /function/opentelemetry-javaagent.jar /function/app/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS "-javaagent:/function/app/opentelemetry-javaagent.jar -Dotel.resource.attributes=service.name=second-function -Dotel.instrumentation.opentelemetry-api.enabled=true -Dotel.instrumentation.opentelemetry-instrumentation-annotations.enabled=true"
# CMD ["java", "-javaagent:opentelemetry-javaagent.jar", "-Dotel.resource.attributes=service.name=test-service", "-jar", "/test-service.jar"]
CMD ["com.example.fn.SecondFunction::handleRequest"]