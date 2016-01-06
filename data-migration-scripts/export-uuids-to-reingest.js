var CONTENT_TO_REINGEST_COLLECTION = "content_to_reingest";

db.getCollection(CONTENT_TO_REINGEST_COLLECTION).find({}).forEach(function (doc){
    print(doc.uuid);
});