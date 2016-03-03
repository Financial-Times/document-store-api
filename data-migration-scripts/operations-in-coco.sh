#!/usr/bin/env bash

PREFER_ARCHIVE=$1

echo "Moving scripts and data to container"
sudo mv dump restore-data.js operations-in-container.sh /vol/mongodb

echo "Running operations in MongoDB docker container"
MONGO_CONTAINER_ID="$(docker ps | grep "coco/mongodb" | cut -d' ' -f1)"
docker exec $MONGO_CONTAINER_ID data/db/operations-in-container.sh $PREFER_ARCHIVE

echo "Cleaning Coco"
sudo rm -r /vol/mongodb/dump /vol/mongodb/restore-data.js /vol/mongodb/operations-in-container.sh /home/core/operations-in-coco.sh


