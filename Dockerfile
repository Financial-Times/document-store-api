FROM coco/dropwizardbase-internal:v1.0.3

ADD . /
ARG SONATYPE_USER=${SONATYPE_USER}
ARG SONATYPE_PASSWORD=${SONATYPE_USER}

RUN apk --update add git libstdc++ \
  && HASH=$(git log -1 --pretty=format:%H) \
  && BUILD_NUMBER=$(cat buildnum.txt) \
  && BUILD_URL=$(cat buildurl.txt) \
  && echo "DEBUG Jenkins job url: $BUILD_URL" \
  && mvn install -Dbuild.git.revision=$HASH -Dbuild.number=$BUILD_NUMBER -Dbuild.url=$BUILD_URL -Djava.net.preferIPv4Stack=true \
  && rm target/document-store-api-*-sources.jar \
  && mv target/document-store-api-*.jar document-store-api.jar \
  && apk del go git \
  && rm -rf /var/cache/apk/* /target* /root/.m2/*

EXPOSE 8080 8081

CMD exec java $JAVA_OPTS \
         -Ddw.server.applicationConnectors[0].port=8080 \
         -Ddw.server.adminConnectors[0].port=8081 \
         -Ddw.mongo.addresses=$MONGO_ADDRESSES \
         -Ddw.cacheTtl=$CACHE_TTL \
		 -Ddw.apiHost=$API_HOST \
		 -Ddw.logging.appenders[0].logFormat="%-5p [%d{ISO8601, GMT}] %c: %X{transaction_id} %replace(%m%n[%thread]%xEx){'\n', '|'}%nopex%n" \
		 -jar document-store-api.jar server config.yaml
