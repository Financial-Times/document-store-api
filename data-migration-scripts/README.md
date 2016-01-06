# Data migration scripts

This readme file describe how to migrate lists and content between from UCS to COCO.
The migration works with if the following assumptions are respected:

* articles can be published, updated, deleted and republished with the same UUID;
* other pices of content (e.g. images) can be published once, but not updated or deleted.     
 
The procedure to migrate data is the following:      
 
1.  Get public IP of the coco instance that hosts the primary MongoDB container. 

    ``
    ssh -A -l core <coco-tunnel>
    fleetctl ssh mongodb@<number-of-primary-instance>.service
    curl http://169.254.169.254/latest/meta-data/public-ipv4   
    ``
   
2.  Start migration process on you localhost:
    
    ``
    ./migrate-ucs-coco.sh <ucs-mongodb-host> <public-ip-coco-primary>
    ``

3.  Reingest articles that can be removed or modified during the migration.
    You will find the list of UUIDs is in the `content_to_reingest` collection. 