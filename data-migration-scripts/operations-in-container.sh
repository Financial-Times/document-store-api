#!/usr/bin/env bash

mongorestore -h localhost -d upp-store -c content_archive /data/db/dump/upp-store/content.bson
mongorestore -h localhost -d upp-store -c lists_archive /data/db/dump/upp-store/lists.bson
mongo upp-store --eval="var PREFER_ARCHIVE=$1" /data/db/restore-data.js