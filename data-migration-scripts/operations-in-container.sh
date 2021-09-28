#!/usr/bin/env bash

mongorestore -h localhost -d upp-store -c content_archive /data/db/dump/upp-store/content.bson
mongo upp-store --eval="var PREFER_ARCHIVE=$1" /data/db/restore-data.js