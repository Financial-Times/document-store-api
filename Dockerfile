FROM up-registry.ft.com/coco/dropwizardbase

RUN git clone http://git.svc.ft.com:8080/scm/cp/document-store-api.git

ADD buildnum.txt /
ADD buildurl.txt /
ADD imageversion.txt /
RUN cd document-store-api && HASH=$(git log -1 --pretty=format:%H) && mvn install -Dbuild.number=$(echo $(awk '{ print }' /buildnum.txt)) -Dbuild.url=$(echo $(awk '{ print }' /buildurl.txt)) -Dbuild.git.revision=$HASH -Dimage.version=$(echo $(awk '{ print }' /imageversion.txt))
RUN cp /document-store-api/target/document-store-api-0.0.1-SNAPSHOT.jar /app.jar

ADD config.yaml /

CMD java -Ddw.mongo.addresses=$MONGO_ADDRESSES -Ddw.apiPath=$API_PATH -jar app.jar server config.yaml

