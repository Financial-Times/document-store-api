#!/usr/bin/env bash

echo "Moving scripts and data to container"
sudo mv dump restore-data.js operations-in-container.sh export-uuids-to-reingest.js /vol/mongodb

echo "Running operations in MongoDB docker container"
MONGO_CONTAINER_ID="$(docker ps | grep "coco/mongodb" | cut -d' ' -f1)"
docker exec $MONGO_CONTAINER_ID "/data/db/operations-in-container.sh"

sudo mv /vol/mongodb/uuids-to-reingest.txt /home/core

echo "Cleaning Coco"
sudo rm -r /vol/mongodb/dump /vol/mongodb/restore-data.js /vol/mongodb/operations-in-container.sh /vol/mongodb/export-uuids-to-reingest.js /home/core/operations-in-coco.sh


