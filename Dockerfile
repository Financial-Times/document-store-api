FROM up-registry.ft.com/coco/dropwizardbase

ADD . /

RUN apk --update add git \
  && HASH=$(git log -1 --pretty=format:%H) \
  && mvn install -Dbuild.number=$(echo $(awk '{ print }' /buildnum.txt)) -Dbuild.url=$(echo $(awk '{ print }' /buildurl.txt)) -Dbuild.git.revision=$HASH -Dimage.version=$(echo $(awk '{ print }' /imageversion.txt)) \
  && mv /target/document-store-api-0.0.1-SNAPSHOT.jar /app.jar \
  && apk del go git \
  && rm -rf /var/cache/apk/* /target* /root/.m2/*

CMD java -Ddw.mongo.addresses=$MONGO_ADDRESSES -Ddw.apiHost=$API_HOST -jar app.jar server config.yaml

