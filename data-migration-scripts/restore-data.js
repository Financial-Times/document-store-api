var SOURCE_CONTENT_COLLECTION = "content_archive";
var CONTENT_COLLECTION= "content";
var CONTENT_TO_REINGEST_COLLECTION = "content_to_reingest";
var REIMPORTED_CONTENT_COLLECTION = "content_reimported"

var SOURCE_LISTS_COLLECTION = "lists_archive";
var LISTS_COLLECTION= "lists";

var NOTIFICATIONS_COLLECTION = "notifications";

var BACKUP_SUFFIX = "_old";
var CONTENT_MIGRATION_TIME = new ISODate();

function getUuidsInNotificationsBetween(startDate, endDate){
	return db.getCollection(NOTIFICATIONS_COLLECTION).distinct("uuid",{"changeDate" : {$gte : startDate, $lte : endDate}});
}

function createCollectionOfArticlesToBeReingestedBetween(startDate, endDate){
	print("Creating collection of articles to be reingested");
	var uuids = getUuidsInNotificationsBetween(startDate, endDate);
	uuids.forEach(function(uuid){
		db.getCollection(CONTENT_TO_REINGEST_COLLECTION).save({"uuid" : uuid});
	});
}

function getDocumentByUuidFromCollection(uuid,collection){
	var doc = db.getCollection(collection).find({"uuid":uuid}, { _id : 0});
	if(doc.count() == 0) return null;
	if(doc.count() == 1) return doc[0];
	if(doc.count() > 1)	throw "Several documents with same UUID! The Collection is not consistent!";
}

function restoreFromBackup(uuid,backupCollections,targetCollection){
	try {
		var doc = getDocumentByUuidFromCollection(uuid,backupCollections);
		db.getCollection(targetCollection).save(doc);
	} catch (error) {
		print(error);
	}
}

function articleNeedsToBeReingested(uuid){
	db.getCollection(CONTENT_TO_REINGEST_COLLECTION).save(uuid);
}

function contentIsArticle(doc){
	if(doc.realtime != true && doc.brands != null && doc.internalBinaryUrl == null && doc.members == null ){
		return true;
	} else {
		return false;
	}
}

function copyDocumentToCollection(doc,collection){
    db.getCollection(collection).save(doc);
}

function getContentPublishedBetweenInBackup(startDate, endDate){
	return db.getCollection(CONTENT_COLLECTION + BACKUP_SUFFIX).find({"publishedDate" : {$gte : startDate.toISOString(), $lte : endDate.toISOString()}},{ _id : 0});
}

function restoreNonArticlesPublishedBetween(startDate, endDate){
	print("Restoring existing content between " + startDate + " and " + endDate);
	var docs = getContentPublishedBetweenInBackup(startDate, endDate)
	print(docs.count());
	var progressStatus = 0;
	docs.forEach(function(doc){
		if(!contentIsArticle(doc)) {
			db.getCollection(CONTENT_COLLECTION).save(doc);
			db.getCollection(REIMPORTED_CONTENT_COLLECTION).save(doc);
			progressStatus++;
	    	if(progressStatus % 100 == 0) {
	       		print("Populated content documents: " + progressStatus);
			}
	    }
	});
	print("Populated content documents: " + progressStatus);
}

function dropDocumentWithUuidInCollection(uuid,collection){
	db.getCollection(collection).remove({"uuid" : uuid});
}

function articleIsModifiedAfter(date, uuid){
	return db.getCollection(NOTIFICATIONS_COLLECTION).find({"uuid" :uuid, "changeDate" : {$gte : date}}).count() > 0;
}

function dropArticlesModifiedBetween(startDate, endDate) {
	print("Dropping content modified between " + startDate + " and " + endDate);
	var uuids = getUuidsInNotificationsBetween(startDate, endDate);
	var progressStatus = 0;
	uuids.forEach(function(uuid){
		if(!articleIsModifiedAfter(endDate, uuid)) {
			dropDocumentWithUuidInCollection(uuid,CONTENT_COLLECTION);
			progressStatus++;
	    	if(progressStatus % 100 == 0) {
	       		print("Dropped articles: " + progressStatus);
			}
		}
	});
	print("Dropped articles: " + progressStatus);
}

function getLastPublishingDateFrom(collection){
	var lastPublishingDate = db.getCollection(collection).find({}).sort({publishedDate : -1}).limit(1).toArray()[0].publishedDate;
	return new ISODate(lastPublishingDate);
}

function getListsFromBackup(){
	return db.getCollection(LISTS_COLLECTION + BACKUP_SUFFIX).find({},{ _id : 0});
}

function updateListsModifiedInBackup(){
	print("Updating lists modified in backup");
	var lists = getListsFromBackup();
	var progressStatus = 0;
	lists.forEach(function(list){
		dropDocumentWithUuidInCollection(list.uuid,LISTS_COLLECTION);
		restoreFromBackup(list.uuid,LISTS_COLLECTION + BACKUP_SUFFIX, LISTS_COLLECTION);
		progressStatus++;
	    if(progressStatus % 100 == 0) {
	        print("Updated lists: " + progressStatus);
		}
	});
	print("Updated lists: " + progressStatus);
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

    // OPERATIONS ON LISTS

    // Make a backup of the "lists" collection by renaming it as "lists_old"
	move(LISTS_COLLECTION, LISTS_COLLECTION + BACKUP_SUFFIX);

	// The documents from the archive collection are moved to the "lists" collection
	mergeDocuments(SOURCE_LISTS_COLLECTION, LISTS_COLLECTION);

	// The following function restores lists copied to "lists_old".
	updateListsModifiedInBackup();


    // OPERATIONS ON CONTENT

    // Make a backup of the "content" collection by renaming it as "content_old"
	move(CONTENT_COLLECTION, CONTENT_COLLECTION + BACKUP_SUFFIX);

    // The following variable represents the moment in which the migration of content data is started
	CONTENT_MIGRATION_TIME = new ISODate();
    print("Content migration started at " + CONTENT_MIGRATION_TIME);

	// The documents from the archive collection are moved to the "content" collection
	mergeDocuments(SOURCE_CONTENT_COLLECTION, CONTENT_COLLECTION);

	// The newt two lines drop all the articles that have been modified between the publishing of the last piece of
	//content in the archive and the start of the content migration.
	// During such period, articles in the archive can be modified, therefore they are out-of-date.
    var lastPublishingDateSourceContentCollection = getLastPublishingDateFrom(SOURCE_CONTENT_COLLECTION);
	dropArticlesModifiedBetween(lastPublishingDateSourceContentCollection,CONTENT_MIGRATION_TIME);

    // The next two lines restore all non-articles copied to "content_old" between the publishing of the last piece of
    // content in the archive and the publishing of the last piece of content in "content_old".
    // Pieces of content that are not articles (e.g., images) are published once and then they are not modified or deleted.
    // It implies that "content_old" contains consistent data about non-articles that are missing in archive during
    // the its migration from a different host
    var lastPublishingDateBackupContentCollection = getLastPublishingDateFrom(CONTENT_COLLECTION + BACKUP_SUFFIX);
    restoreNonArticlesPublishedBetween(lastPublishingDateSourceContentCollection,lastPublishingDateBackupContentCollection);

    // Articles can be updated or deleted between this process of data migration. This means that manipulation of
    // such articles can make their data inconsistent.
    // The only way to make sure that articles are restored properly from the archive is to manually reingest them.
    // The following function creates the collection of article UUIDs to be reingested in order to have consistent data.
	createCollectionOfArticlesToBeReingestedBetween(lastPublishingDateSourceContentCollection,CONTENT_MIGRATION_TIME);
}

restoreData();