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

function populateExistingContentBetween(startDate, endDate){
	print("Populating existing content between " + startDate + " and " + endDate);
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

function sameListIsInCollection(list, collection){
	return db.getCollection(collection).find(list).count() > 0 ;
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
		if(!sameListIsInCollection(list, LISTS_COLLECTION)){
			dropDocumentWithUuidInCollection(list.uuid,LISTS_COLLECTION);
			restoreFromBackup(list.uuid,LISTS_COLLECTION + BACKUP_SUFFIX, LISTS_COLLECTION);
			progressStatus++;
	    	if(progressStatus % 100 == 0) {
	       		print("Updated lists: " + progressStatus);
			}
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

function backup(collection){
    print("Backup of '"+collection+"' collection for rollback")
	db.getCollection(collection).renameCollection(collection + BACKUP_SUFFIX, true)
	if(collection == CONTENT_COLLECTION){
		CONTENT_MIGRATION_TIME = new ISODate();
		print("Content migration started at " + CONTENT_MIGRATION_TIME);
	}
}

function restoreData(){
	backup(LISTS_COLLECTION);
	mergeDocuments(SOURCE_LISTS_COLLECTION, LISTS_COLLECTION);
	updateListsModifiedInBackup();

	backup(CONTENT_COLLECTION);
	mergeDocuments(SOURCE_CONTENT_COLLECTION, CONTENT_COLLECTION);
    var lastPublishingDateSourceContentCollection = getLastPublishingDateFrom(SOURCE_CONTENT_COLLECTION);
	var lastPublishingDateBackupContentCollection = getLastPublishingDateFrom(CONTENT_COLLECTION + BACKUP_SUFFIX);
	dropArticlesModifiedBetween(lastPublishingDateSourceContentCollection,CONTENT_MIGRATION_TIME);
    populateExistingContentBetween(lastPublishingDateSourceContentCollection,lastPublishingDateBackupContentCollection);
	createCollectionOfArticlesToBeReingestedBetween(lastPublishingDateSourceContentCollection,CONTENT_MIGRATION_TIME);
}

restoreData();