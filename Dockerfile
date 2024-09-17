FROM amazoncorretto:8u412-alpine3.16

ADD .git/ /.git/
ADD . /document-store-api/
ADD pom.xml /

ARG CLOUDSMITH_USER
ARG CLOUDSMITH_PASSWORD
ARG GIT_TAG
ARG GITHUB_TOKEN

ENV MAVEN_HOME=/root/.m2
ENV TAG=$GIT_TAG

RUN apk --update add git maven curl \
  # Set Cloudsmith credentials in settings.xml file
  && mkdir $MAVEN_HOME \
  && curl -H "Authorization: token $GITHUB_TOKEN" -H "Accept: application/vnd.github.v3.raw" -o "$MAVEN_HOME/settings.xml" -L "https://raw.githubusercontent.com/Financial-Times/cloudsmith-settings/main/public-settings.xml" \
  # Generate docker tag
  && cd /document-store-api \
  && HASH=$(git log -1 --pretty=format:%H) \
  && TAG=$(git tag --sort=committerdate | tail -1) \
  && VERSION=${TAG:-untagged} \
  # Set Maven artifact version
  && mvn clean versions:set -DnewVersion=$VERSION \
  # Build project without tests
  && mvn -e clean package -Dbuild.git.revision=$HASH -Djava.net.preferIPv4Stack=true -DskipTests \
  # Remove sources jar
  && rm target/document-store-api-*-sources.jar \
  # Remove version from executable jar name
  && mv target/document-store-api-*.jar /document-store-api.jar \
  # Move resources to root directory in docker container
  && mv config.yaml /config.yaml \
  && mv data-migration-scripts* /data-migration-scripts \
  # Clean up unnecessary dependencies and binaries
  && apk del go git maven \
  && rm -rf /var/cache/apk/* /document-store-api/target* /root/.m2/* /tmp/*.apk

FROM eclipse-temurin:8u345-b01-jre
COPY --from=0 /document-store-api.jar /document-store-api.jar
COPY --from=0 /config.yaml /config.yaml

EXPOSE 8080 8081

CMD exec java $JAVA_OPTS \
  -Ddw.server.applicationConnectors[0].port=8080 \
  -Ddw.server.adminConnectors[0].port=8081 \
  -Ddw.mongodb.address=$DB_CLUSTER_ADDRESS \
  -Ddw.mongodb.username=$DB_USERNAME \
  -Ddw.mongodb.password=$DB_PASSWORD \
  -Ddw.cacheTtl=$CACHE_TTL \
  -Ddw.apiHost=$API_HOST \
  -Ddw.logging.appenders[0].logFormat="%m%n" \
  -DgitTag=$TAG \
  -jar document-store-api.jar server config.yaml
