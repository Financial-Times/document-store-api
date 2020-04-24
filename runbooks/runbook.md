# UPP - Document Store API

The Document Store API writes to and reads from Document Store (MongoDB) in the Delivery cluster.

## Primary URL

https://upp-prod-delivery-glb.upp.ft.com/__document-store-api/

## Service Tier

Platinum

## Lifecycle Stage

Production

## Delivered By

content

## Supported By

content

## Known About By

- hristo.georgiev
- elina.kaneva
- georgi.ivanov
- georgi.kazakov
- tsvetan.dimitrov
- robert.marinov
- kalin.arsov
- mihail.mihaylov
- boyko.boykov
- dimitar.terziev

## Host Platform

AWS

## Architecture

Document Store API allows writes to and reads from MongoDB. Endpoints: `/content` and `/lists`. If this service is unavailable no publishes nor reads from the APIs will be working as expected.

## Contains Personal Data

No

## Contains Sensitive Data

No

## Dependencies

- upp-mongodb
- public-concepts-api
- public-concordances-api

## Failover Architecture Type

ActiveActive

## Failover Process Type

FullyAutomated

## Failback Process Type

FullyAutomated

## Failover Details

The service is deployed in both Delivery clusters. The failover guide for the cluster is located here:
https://github.com/Financial-Times/upp-docs/tree/master/failover-guides/delivery-cluster

## Data Recovery Process Type

NotApplicable

## Data Recovery Details

The service does not store data, so it does not require any data recovery steps.

## Release Process Type

PartiallyAutomated

## Rollback Process Type

Manual

## Release Details

Manual failover is needed when a new version of the service is deployed to production. Otherwise, an automated failover is going to take place when releasing.
For more details about the failover process please see: https://github.com/Financial-Times/upp-docs/tree/master/failover-guides/delivery-cluster

## Key Management Process Type

Manual

## Key Management Details

To access the service clients need to provide basic auth credentials.
To rotate credentials you need to login to a particular cluster and update varnish-auth secrets.

## Monitoring

Service in UPP K8S delivery clusters:
- Delivery-Prod-EU health: https://upp-prod-delivery-eu.upp.ft.com/__health/__pods-health?service-name=document-store-api
- Delivery-Prod-US health: https://upp-prod-delivery-us.upp.ft.com/__health/__pods-health?service-name=document-store-api

## First Line Troubleshooting

https://github.com/Financial-Times/upp-docs/tree/master/guides/ops/first-line-troubleshooting

## Second Line Troubleshooting

Please refer to the GitHub repository README for troubleshooting information.
