# jsongen.py

A script that generates a JSON document that can be posted to document-store-api to publish lists

### Table of Contents
**[Usage - CLI](#usage-cli)**  
**[Usage - Jenkins job](#usage-jenkins-job)**
**[Support](#support)** 

## Usage - CLI

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

## Usage - Jenkins job

Parameterized build job on Jenkins can be used to gather parameter values and pass them on to the jsongen.py script.

Build job is available [on ftjen02441-lvpr-uk-p](http://ftjen02441-lvpr-uk-p:8181/view/All/job/Document-store-api%20-%20LIST%20PUT/).

Steps to publish a list using the above Jenkins job:

 1. Click [Build with Paramters](http://ftjen02441-lvpr-uk-p:8181/view/All/job/Document-store-api%20-%20LIST%20PUT/build?delay=0sec)
 2. Fill out the following fields
 * __LIST_ID__: The UUID of the list to be published
 * __TITLE__: The title of the  list
 * __CONTENT_IDS__: A comma-separated list of content IDs
 * __LAYOUT_HINT__: The type of a layout hint, e.g. standard
 * __TRANSACTION_ID__: ID of th transaction, e.g.  tid_dj1z4nj1i0
 * __ENDPOINT__: URL of the endpoint where there request should be sent to
 3. If publishing into production make sure to tick the box __PROD_RELEASE__ 
 * This checkbox is there to prevent accidental publishing into production environment
 


 
 ## Support
 
jsongen.py was written by jussi.heinonen@ft.com. Contact him in case script has any behavioural issues.
 