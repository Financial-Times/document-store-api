[![CircleCI](https://dl.circleci.com/status-badge/img/gh/Financial-Times/document-store-api/tree/master.svg?style=svg&circle-token=50fd48d00c0ff0bc1ed767a61331f7b3d5f8fadf)](https://dl.circleci.com/status-badge/redirect/gh/Financial-Times/document-store-api/tree/master)
[![Coverage Status](https://coveralls.io/repos/github/Financial-Times/document-store-api/badge.svg)](https://coveralls.io/github/Financial-Times/document-store-api)

# Document Store API

Document Store API is a Dropwizard application which allows **writes to** and **reads from** Atlas MongoDB.

The `/content/{uuid}` endpoint is for reading from mongo without any other restriction, no formatting, no hard-coded layout or classes. It's a pure document representation of what is stored in Atlas MongoDB.

The `/complementarycontent/{uuid}` endpoint is used for reading promotional fields of stored content.

The `/internalcomponents/{uuid}` endpoint is used for reading internal fields of stored content.

## Running locally

To compile and build jar

```sh
mvn clean package -Djava.net.preferIPv4Stack=true -Dmaven.test.skip=true
```

To run all tests, you should remove the flag `-Dmaven.test.skip=true` when running maven package. If you don't want to run integration tests, run with `-Dshort` flag
```sh
mvn clean package -Djava.net.preferIPv4Stack=true -Dshort
```

To run all tests:

```
# Run local instance of MongoDB needed for some tests.
docker-compose up -d mongodb

# Execute all tests.
MONGO_TEST_URL=localhost:27017 -Djava.net.preferIPv4Stack=true mvn clean package
```

To run locally:
1. Use `docker-compose` to spin it up locally in a container:
    ```sh
    docker-compose up -d mongodb
    ```

1. execute the following to run the application:
    ```sh
    java -jar target/document-store-api-0.0.1-SNAPSHOT.jar server config-local.yml
    ```

## Building/Deploying

**N.B.** As of 18-10-2016, there should be no further deployments to **UCS**.

To build the Docker image locally, you now need to pass build arguments to authenticate against our Cloudsmith server:

docker build -t coco/document-store-api --build-arg CLOUDSMITH_USER=content-programme-read-write --build-arg CLOUDSMITH_PASSWORD=AvailableIn1Pass .


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

## Adding support for a new resource type

If you need to add a new resource, add in DocumentStoreApiApplication a new chain handler for the wanted operations.
Handler and Targets can be reused in multiple chains.

## Healthchecks and GTG

There are healthchecks for
- connection to Atlas MongoDB
- index state of Atlas MongoDB collections

Only the connection healthcheck influences GTG responses. Whenever a change is detected in the connection state, the application may move between states in the following state chart.
![state chart](https://www.lucidchart.com/publicSegments/view/773931fc-d21d-44c2-a84f-b89d8508d930/image.jpeg)

## Logging
This service uses the savoirtech slf4j-json-logger library. Although this is mostly a drop-in replacement for SLF4J, events are queried by monitoring tools from Splunk and the logging pattern should be left as `%m%n` as described in [their README](https://github.com/savoirtech/slf4j-json-logger#logging-configuration).
