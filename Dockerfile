FROM openjdk:8u212-jdk-alpine3.9

ADD .git/ /.git/
ADD . /document-store-api/

ARG SONATYPE_USER
ARG SONATYPE_PASSWORD

RUN apk --update add git maven curl \
  && rm -rf /root/.m2/ \
  && mkdir /root/.m2/ \
  && curl -v -o /root/.m2/settings.xml "https://raw.githubusercontent.com/Financial-Times/nexus-settings/master/public-settings.xml" \
  && cd document-store-api \
  && HASH=$(git log -1 --pretty=format:%H) \
  && TAG=$(git tag -l --points-at $HASH) \
  && VERSION=${TAG:-untagged} \
  && mvn versions:set -DnewVersion=$VERSION \
  && mvn clean package -Dbuild.git.revision=$HASH -Djava.net.preferIPv4Stack=true -Dmaven.test.skip=true \
  && rm target/document-store-api-*-sources.jar \
  && mv target/document-store-api-*.jar /document-store-api.jar \
  && mv config.yaml /config.yaml \
  && mv data-migration-scripts* /data-migration-scripts \
  && mv scripts* /scripts \
  && apk del git maven curl \
  && rm -rf /var/cache/apk/* /document-store-api/target/* /root/.m2/* /tmp/*.apk

EXPOSE 8080 8081

CMD exec java $JAVA_OPTS \
         -Ddw.server.applicationConnectors[0].port=8080 \
         -Ddw.server.adminConnectors[0].port=8081 \
         -Ddw.mongo.addresses=$MONGO_ADDRESSES \
         -Ddw.cacheTtl=$CACHE_TTL \
		 -Ddw.apiHost=$API_HOST \
		 -Ddw.logging.appenders[0].logFormat="%m%n" \
		 -jar document-store-api.jar server config.yaml
