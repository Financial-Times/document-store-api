# Scripts for data migration from UCS to COCO

This readme file describes how to migrate lists and content between from UCS to COCO.
The migration scripts assume the following:

* articles can be published, updated, deleted and republished with the same UUID;
* other pieces of content (e.g. images) can be published once, but not updated or deleted;
* the `notification` collection in COCO is up-to-date.
 
The procedure to migrate data is the following:      
 
1.  Get public IP of the coco instance that hosts the primary MongoDB container. 

	```bash
    ssh -A -l core <coco-tunnel>
    fleetctl ssh mongodb@<number-of-primary-instance>.service
    curl http://169.254.169.254/latest/meta-data/public-ipv4
    ```
    
    The output of `curl` contains the public IP address.
    
2.  Stop the deployer service in COCO:
    
    ```
    fleetctl stop deployer.service
    ```
3.  Stop publish-availability-monitor serice in COCO:

    ```
    fleetctl stop publish-availability-monitor@1.service
    ```

4.  Stop all the content ingester services in COCO:
    
    ```
    fleetctl stop content-ingester@1.service
    fleetctl stop content-ingester@2.service
    ...
    fleetclt stop content-ingester@<n>.service
    ```
   
5.  Run the migration process on your localhost:
    `prefer-archive`: bool flag. set this to `true` if you want the archived version of content to be preferred over the existing one. otherwise the existing one will be restored.
    
    ```
    ./migrate-ucs-coco.sh <ucs-mongodb-secondary-host> <public-ip-coco-primary> <prefer-archive>
    ```

6.  Restart the deployer service in COCO:
    
    ```
    fleetclt start deployer.service
    ```
