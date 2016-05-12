# Document Store API
Document Store API is a Dropwizard application which allows writes to and reads from MongoDB.

Reading content by API users should be done via the /content-read/{uuid} endpoint. This makes sure the format of the response obeys the contract that our api was designed by.

The /content/{uuid} endpoint is for reading from mongo without any other restriction, no formatting, no hard-coded layout or classes. It's a pure document representation of what is stored in mongoDB.

These two endpoints should be separated into two independent applications, all of them using the name /content, one reading, one formatting, but that was scheduled to be later, for now both layers are in the same app.

Operations on lists DOES NOT share the same logic, their read/writes are separate from this mechanism.

## Running locally
To compile, run tests and build jar
    
    mvn clean install 

To run locally, run:
    
    java -jar target/document-store-api-0.0.1-SNAPSHOT.jar server config-local.yml

## Building/deploying
Check in, push, and wait three minutes: [this Jenkins job](http://ftjen06609-lvpr-uk-p:8181/job/document-store-api/) will build and package the application. 

The app will be deployed to Int automatically on successful build.

Deploys to Test and Prod can also be done via Jenkins jobs on the [same jenkins instance] (http://ftjen06609-lvpr-uk-p:8181).

## Content PUT
Make a PUT request to http://localhost:14180/content/{uuid} with Content-Type set to application/json.

Body should look like:

    {
        "uuid": "3b7b7702-debf-11e4-b9ec-00144feab7de",
        "title": "Ukraine looks to foreign-born ministers to kick-start reform",
        "titles": null,
        "byline": "Roman Olearchyk, Kiev, and Neil Buckley, London",
        "brands": [
            {
                "id": "http://api.ft.com/things/dbb0bdae-1f0c-11e4-b0cb-b2227cce2b54"
            }
        ],
        "identifiers": [
            {
                "authority": "http://api.ft.com/system/FTCOM-METHODE",
                "identifierValue": "3b7b7702-debf-11e4-b9ec-00144feab7de"
            }
        ],
        "publishedDate": "2015-04-15T08:33:02.000Z",
        "body": "<body><content data-embedded=\"true\" id=\"937885ac-e2bc-11e4-347b-978e959e1c97\" type=\"http://www.ft.com/ontology/content/ImageSet\"></content><p>Late last year, US-born Natalie Jaresko was in the Kiev office of the investment firm she founded in 2006 when headhunters came to call. Within days she was named as finance minister of war-torn country.</p></body>",
        "description": null,
        "mediaType": null,
        "pixelWidth": null,
        "pixelHeight": null,
        "internalBinaryUrl": null,
        "members": null,
        "mainImage": "937885ac-e2bc-11e4-347b-978e959e1c97"
    }

Any fields that aren't supported will be ignored. NB: this response body is the same as the response for a GET to a content transformer.

## Content DELETE
Make a DELETE request to http://localhost:14180/content/{uuid} with Content-Type set to application/json.

## Content GET
Make a GET request to `http://localhost:14180/content/{uuid}` with `Content-Type` set to `application/json`.

Should return the json right in the format as you PUT it in.

## Content Read GET
Make a GET request to `http://localhost:14180/content-read/{uuid}` with `Content-Type` set to `application/json`.

Should return the json in the format of APIv2.

## Content query by identifier
Make a GET reqest to `http://localhost:14180/content-query?identifierAuthority={authority}&identifierValue={identifierValue}`. The combination of `authority` and `identifierValue` should be expected to produce a unique result.

Example request:
`GET /content-query?identifierAuthority=http://api.ft.com/system/FT-LABS-WP-1-335&identifierValue=http://www.ft.com/fastft/2015/12/09/south-african-rand-dives-after-finance-ministers-exit/ HTTP/1.1`

The possible HTTP responses are:
* `301`: A single document was found matching the query; its URI may be found in the `Location` header.
* `404`: No match was found.
* `500`: Multiple matches were found (this is not expected).

## List PUT
Make a PUT request to http://localhost:14180/lists/{uuid} with Content-Type set to application/json.

Body should look like:

    { 
        "uuid": "3c99c2ba-a6ae-11e2-95b1-00144feabdc0",
        "title": "Test List",
        "concept": {
            "tmeIdentifier": "NzE=-U2VjdGlvbnM=",
            "prefLabel": "Markets"
        },
        "type": {
            "id": "http://api.ft.com/things/c5de8687-c49f-4904-bbc9-bff4f55e50a0",
            "prefLabel": "Opinion & Analysis"
        },
        "items" : [
            {
                "uuid": "0237b884-d124-11e2-be7b-00144feab7de"
            },
            {
                "uuid": "68340f9f-67c3-33cd-97b1-f07bea7ce714"
            },
            {
                "webUrl": "http://video.ft.com/3887333300001/Thames-tour-shows-changing-London-skyline/life-and-arts"
            }
        ]
    }
    
Any fields that aren't supported will be ignored. NB: this response body is the same as the response for a GET to a list transformer.

## List GET
Make a GET request to http://localhost:14180/lists/{uuid} with Content-Type set to application/json.

## List GET by Concept and Type
Make a GET request to http://localhost:14180/lists?concept=NzE=-U2VjdGlvbnM=&type=c5de8687-c49f-4904-bbc9-bff4f55e50a0 with Content-Type set to application/json.

You should get a single result back. If there was more than one match, one will be returned and an error will be logged.

## List DELETE
Make a DELETE request to http://localhost:14180/lists/{uuid} with Content-Type set to application/json.

## Adding support for a new resource type
Currently you need to:
1. Add a new model class or classes, extending Document
2. Add new resource endpoints in DocumentResource, for PUT, GET and DELETE. Call the helper methods.

## Does this trigger the container?