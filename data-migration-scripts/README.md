# Scripts for data migration from UCS to COCO

This readme file describes how to migrate lists and content between from UCS to COCO.
The migration scripts assume the following:

* articles can be published, updated, deleted and republished with the same UUID;
* other pices of content (e.g. images) can be published once, but not updated or deleted;
* the `notification` collection in COCO is up-to-date.
 
The procedure to migrate data is the following:      
 
1.  Get public IP of the coco instance that hosts the primary MongoDB container. 

	```bash
    ssh -A -l core <coco-tunnel>
    fleetctl ssh mongodb@<number-of-primary-instance>.service
    curl http://169.254.169.254/latest/meta-data/public-ipv4
    ```
    
    The output of `curl` contains the public IP address.
   
2.  Run the migration process on your localhost:
    
    ```
    ./migrate-ucs-coco.sh <ucs-mongodb-host> <public-ip-coco-primary>
    ```

3.  Reingest articles that can be removed or modified during the migration.
    You will find the list of UUIDs in both:
    
    * the `content_to_reingest` collection in the COCO MongoDB instance;
    * the `uuids-to-reingest.txt` file in the working directory in which you run the migration script.