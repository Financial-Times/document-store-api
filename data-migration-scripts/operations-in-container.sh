#!/usr/bin/env bash

mongorestore -h localhost -d upp-store -c content_archive /data/db/dump/upp-store/content.bson
mongorestore -h localhost -d upp-store -c lists_archive /data/db/dump/upp-store/lists.bson
mongo upp-store /data/db/restore-data.js
mongo upp-store /data/db/export-uuids-to-reingest.js > /data/db/uuids-to-reingest.txt