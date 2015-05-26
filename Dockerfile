FROM coco/javabase

RUN git clone --branch flexibleMongoAddresses http://git.svc.ft.com:8080/scm/cp/document-store-api.git

RUN cd document-store-api && mvn install
RUN cp /document-store-api/target/document-store-api-0.0.1-SNAPSHOT.jar /app.jar

ADD config.yaml /

CMD java -Ddw.mongo.addresses=$MONGO_ADDRESSES -Ddw.apiPath=$API_PATH -jar app.jar server config.yaml

