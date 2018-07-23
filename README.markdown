[![CircleCI](https://circleci.com/gh/Financial-Times/document-store-api.svg?style=svg)](https://circleci.com/gh/Financial-Times/document-store-api) [![Coverage Status](https://coveralls.io/repos/github/Financial-Times/document-store-api/badge.svg)](https://coveralls.io/github/Financial-Times/document-store-api)

# Document Store API

Document Store API is a Dropwizard application which allows writes to and reads from MongoDB.

Reading content by API users should be done via the /content-read/{uuid} endpoint. This makes sure the format of the response obeys the contract that our API was designed by.

The /content/{uuid} endpoint is for reading from mongo without any other restriction, no formatting, no hard-coded layout or classes. It's a pure document representation of what is stored in mongoDB.

These two endpoints should be separated into two independent applications, all of them using the name /content, one reading, one formatting, but that was scheduled to be later, for now both layers are in the same app.

Operations on lists DOES NOT share the same logic, their read/writes are separate from this mechanism.

## Running locally

To compile and build jar

```sh
mvn clean package -Djava.net.preferIPv4Stack=true -Dmaven.test.skip=true
```

To run all tests, you should remove the flag `-Dmaven.test.skip=true` when running maven package. If you don't want to run integration tests, run with `-Dshort` flag
```sh
mvn clean package -Djava.net.preferIPv4Stack=true -Dshort
```

To run locally, run:

```sh
java -jar target/document-store-api-0.0.1-SNAPSHOT.jar server config-local.yml
```

## Building/Deploying

**N.B.** As of 18-10-2016, there should be no further deployments to **UCS**.

To build the Docker image locally, you now need to pass build arguments to authenticate against our Nexus server:

    docker build -t coco/document-store-api --build-arg SONATYPE_USER=upp-nexus --build-arg SONATYPE_PASSWORD=AvailableInLastPass .

To build the final image, check in, push, and wait three minutes: [this Jenkins job](http://ftjen06609-lvpr-uk-p:8181/job/document-store-api/) will build and package the application.

The creation of the Docker image for will be automatically triggered after the merge and push in master branch by [this Jenkins job](http://ftaps116-lvpr-uk-d:8080/job/document-store-api/).

## Content PUT

Make a PUT request to `http://localhost:14180/content/{uuid}` with `Content-Type` set to `application/json`.

Body should look like:

```json
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
```

Any fields that aren't supported will be ignored. NB: this response body is the same as the response for a GET to a content transformer.

## Content DELETE

Make a DELETE request to `http://localhost:14180/content/{uuid}` with `Content-Type` set to `application/json`.

## Content GET

### Retrieving an item

Make a GET request to `/content/{uuid}` with `Content-Type` set to `application/json`.

Should return the json right in the format as you PUT it in.

### Retrieving multiple items: Content POST

Make a POST request to: `/{collection}?mget=true`

Body:

```
[
  "fd204ed1-d53d-4b24-99a7-fc62a6778808",
  "4f97b689-d3e4-4ae2-ac34-9abc97a46c28",
  "c86ccb61-38d2-42bd-bb2f-9c69e1fca178"
]
```

Return a JSON array containing the subset of items that were found. In case none are found, it returns an HTTP 200 with an empty array.

### Deprecated way of: Retrieving multiple items

Can't handle too many uuids, the URI has a limit at 2083 characters.

GET `http://localhost:14180/content?uuid={uuid1}&uuid={uuid2}...`.

Return a JSON array containing the subset of items that were found (if none were found, the response will be an empty array).

## Content query by identifier

Make a GET reqest to `http://localhost:14180/content-query?identifierAuthority={authority}&identifierValue={identifierValue}`. The combination of `authority` and `identifierValue` should be expected to produce a unique result.

Example request:
`GET /content-query?identifierAuthority=http://api.ft.com/system/FT-LABS-WP-1-335&identifierValue=http://www.ft.com/fastft/2015/12/09/south-african-rand-dives-after-finance-ministers-exit/ HTTP/1.1`

The possible HTTP responses are:
* `301`: A single document was found matching the query; its URI may be found in the `Location` header.
* `404`: No match was found.
* `500`: Multiple matches were found (this is not expected).

## List PUT

Make a PUT request to `http://localhost:14180/lists/{uuid}` with `Content-Type` set to `application/json`.

Body should look like:

```json
{
    "uuid": "3c99c2ba-a6ae-11e2-95b1-00144feabdc0",
    "title": "Test List",
    "concept": {
        "uuid": "78d878ac-6226-47e4-84b1-39a127e3cc11",
        "prefLabel": "Markets"
    },
    "listType": "TopStories",
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
```

Any fields that aren't supported will be ignored. NB: this response body is the same as the response for a GET to a list transformer.

`concept` and `listType` are optional. If `concept` is supplied, both `uuid` and `prefLabel` fields must be supplied.

As business requirement, multiple lists with same `concept` and `listType` are not allowed in the data store.
A Splunk alert will be triggered when multiple lists are detected.

## List GET

Make a GET request to http://localhost:14180/lists/{uuid} with Content-Type set to application/json.

## List GET by Concept and Type

Make a GET request to http://localhost:14180/lists?curatedTopStoriesFor={concept-uuid} with Content-Type set to application/json.

The query parameter varies according to the type of the list, for example, the following is also supported: curatedOpinionAndAnalysisFor

You should get a single result back. If there was more than one match, one will be returned and an error will be logged.

## List DELETE

Make a DELETE request to http://localhost:14180/lists/{uuid} with Content-Type set to application/json.

## Adding support for a new resource type

If you need to add a new resource, add in DocumentStoreApiApplication a new chain handler for the wanted operations.
Handler and Targets can be reused in multiple chains.
