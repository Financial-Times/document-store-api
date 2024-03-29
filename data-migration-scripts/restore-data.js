var SOURCE_CONTENT_COLLECTION = "content_archive";
var CONTENT_COLLECTION= "content";

var BACKUP_SUFFIX = "_old";

function getDocumentByUuidFromCollection(uuid,collection){
	var doc = db.getCollection(collection).find({"uuid":uuid}, { _id : 0});
	if(doc.count() == 0) return null;
	if(doc.count() == 1) return doc[0];
	if(doc.count() > 1)	throw "Several documents with same UUID! The collection is not consistent!";
}

function restoreFromBackup(uuid,backupCollections,targetCollection){
	try {
		var doc = getDocumentByUuidFromCollection(uuid,backupCollections);
		db.getCollection(targetCollection).save(doc);
	} catch (error) {
		print(error);
	}
}

function dropDocumentWithUuidInCollection(uuid,collection){
	db.getCollection(collection).remove({"uuid" : uuid});
}

function getContentFromBackup() {
    return db.getCollection(CONTENT_COLLECTION + BACKUP_SUFFIX).find({},{ _id : 0});
}

function restoreContentFromBackup(){
    print("Restoring content modified in backup");
   	var content = getContentFromBackup();
   	var progressStatus = 0;
   	content.forEach(function(pieceOfContent){
   		dropDocumentWithUuidInCollection(pieceOfContent.uuid,CONTENT_COLLECTION);
   		restoreFromBackup(pieceOfContent.uuid,CONTENT_COLLECTION + BACKUP_SUFFIX, CONTENT_COLLECTION);
   		progressStatus++;
   	    if(progressStatus % 100 == 0) {
   	        print("Updated content: " + progressStatus);
   		}
   	});
   	print("Updated content: " + progressStatus);
}

function mergeDocuments(sourceCollection, targetCollection){
	print("Merging documents from '" + sourceCollection + "' to '" + targetCollection + "'");
	var progressStatus = 0;
	db.getCollection(sourceCollection).find().forEach(function(doc){
	    db.getCollection(targetCollection).insert(doc);
        progressStatus++;
        if(progressStatus % 1000 == 0) {
            print("Copied documents: " + progressStatus);
        }
	});
	db.getCollection(targetCollection).createIndex({uuid : 1});
	print("Copied documents: " + progressStatus);
}

function move(sourceCollection,targetCollection){
    print("Moving '"+sourceCollection+"' collection to '"+targetCollection+"' collection");
	db.getCollection(sourceCollection).renameCollection(targetCollection, true);
}

function restoreData(){
	print("Existing nr of content: ", db.getCollection(CONTENT_COLLECTION).count());
	print("Archived nr of content: ", db.getCollection(SOURCE_CONTENT_COLLECTION).count());
    // OPERATIONS ON CONTENT
    // Make a backup of the "content" collection by renaming it as "content_old"
	move(CONTENT_COLLECTION, CONTENT_COLLECTION + BACKUP_SUFFIX);

    // Documents from the archive collection are moved to the "content" collection
    mergeDocuments(SOURCE_CONTENT_COLLECTION, CONTENT_COLLECTION);
	print("Total nr of content: ", db.getCollection(CONTENT_COLLECTION).count());

	if (!PREFER_ARCHIVE) {
		// The following function restores recent pieces of contents copied to "content_old".
		restoreContentFromBackup();
	}

}

restoreData();