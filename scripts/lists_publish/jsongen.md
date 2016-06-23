# Publishing Lists via Document Store API

A script [jsongen.py](http://git.svc.ft.com/projects/CP/repos/document-store-api/browse/scripts/lists_publish/jsongen.py) generates a JSON document that can be posted to document-store-api to publish lists. 

Script [jenkins_build_commands.sh](http://git.svc.ft.com/projects/CP/repos/document-store-api/browse/scripts/lists_publish/jenkins_build_commands.sh) contains example of the Curl command that posts JSON document to API.

### Table of Contents
**[Usage - Jenkins job](#usage-jenkins-job)**
**[Usage - CLI](#usage-cli)**  
**[Support](#support)** 

## Usage - Jenkins job

Parameterized build job on Jenkins can be used to gather parameter values and pass them on to the jsongen.py script.

Build job [Document-store-api - LIST PUT is available on ftjen02441-lvpr-uk-p](http://ftjen02441-lvpr-uk-p:8181/view/All/job/Document-store-api%20-%20LIST%20PUT/).

Steps to publish a list using the above Jenkins job:

 1. Click [Build with Paramters](http://ftjen02441-lvpr-uk-p:8181/view/All/job/Document-store-api%20-%20LIST%20PUT/build?delay=0sec)
 2. Fill out the following fields
 * __LIST_ID__: The UUID of the list to be published
   * _Example: 520ddb76-e43d-11e4-9e89-00144feab7de_
 * __TITLE__: The title of the  list
   * _Example: UK Top Stories_
 * __CONTENT_IDS__: A comma-separated list of content IDs
   * _Example: 8405dc04-40a0-11e5-9abe-5b335da3a90e, f102aa60-40c5-11e5-9abe-5b335da3a90e_
 * __LAYOUT_HINT__: Select appropriate from drop-down menu
   * _Example: standard_
 * __TRANSACTION_ID__: ID of the transaction
   * _Example: tid_jussi_republish_
 * __ENVIRONMENT__: Select appropriate from drop-down menu
   * _Example: Prod_uk_
 * __PROD_RELEASE__: If publishing into __Prod_uk or Prod_us__ make sure to tick this box
   * This checkbox is there to prevent accidental publishing into production environments

## Usage - CLI

__NOTE:__ [jsongen.py](http://git.svc.ft.com/projects/CP/repos/document-store-api/browse/scripts/lists_publish/jsongen.py) script only generates a JSON document in current working directory. Script does not make API call.

```
./jsongen.py -i <List UUID> -c <quoted comma-separated list of Content UUIDs> -l <Layout hint> -t <Title of the list, quoted> -x <Transaction ID> 
```

Running the script with the following argument values produces the JSON document shown in code block below.

 * -i _520ddb76-e43d-11e4-9e89-00144feab7de_
 * -c _"5210a6e2-4074-11e5-9abe-5b335da3a90e,e500b846-40cb-11e5-9abe-5b335da3a90e"_
 * -l _standard_
 * -t _"UK Top Stories"_
 * -x _tid_dj1z4nj1i0_

Example JSON document
 
 ```
 {
   "items" : [ 
       {
           "uuid" : "5210a6e2-4074-11e5-9abe-5b335da3a90e"
       }, 
       {
           "uuid" : "e500b846-40cb-11e5-9abe-5b335da3a90e"
       }
   ],
   "title" : "UK Top Stories",
   "uuid" : "520ddb76-e43d-11e4-9e89-00144feab7de",
   "layoutHint" : "standard",
   "publishReference" : "tid_dj1z4nj1i0",
   "lastModified" : "2016-01-20T17:38:46.738Z"
}
 ```
 
 ## Support
 
Scripts were was written by jussi.heinonen@ft.com. Contact him in case script has any behavioural issues.
 