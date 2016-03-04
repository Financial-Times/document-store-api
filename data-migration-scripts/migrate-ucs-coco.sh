#!/usr/bin/env bash

if [ "$#" -ne 3 ]; then
    echo "Illegal number of parameters"
    echo "Usage migrate-ucs-coco.sh <ucs-primary-mongo-host> <public-IP-of-primary-mongo-coco> <prefer-archive>"
    exit 0
fi

if [ "$3" != "true" ] && [ "$3" != "false" ]; then
    echo "Invalid <prefer-archive> value: [$3]. It must be [true|false]."
    exit 0
fi


UCS_MONGODB_HOST="$1"
COCO_PUBLIC_IP="$2"
PREFER_ARCHIVE=$3

echo -e "Running config:\n\t-mongo: $UCS_MONGODB_HOST\n\t-coco: $COCO_PUBLIC_IP\n\t-prefer-archive: $PREFER_ARCHIVE"

function backupFromUcs {
    # Lock the db
    echo "Making dump of UCS data"
    echo "db.fsyncLock()" | mongo $UCS_MONGODB_HOST/test

    # Dump of the collections
    mongodump -h $UCS_MONGODB_HOST -d upp-store -c lists
    mongodump -h $UCS_MONGODB_HOST -d upp-store -c content

    # Unlock the db
    echo "db.fsyncUnlock()" | mongo $UCS_MONGODB_HOST/test
}

function copyScriptsAndDumpToCoco {
    rsync -avz -r -e ssh dump restore-data.js operations-in-coco.sh operations-in-container.sh core@$COCO_PUBLIC_IP:/home/core
}

function runOperationsInCoco {
    ssh core@$COCO_PUBLIC_IP "chmod 750 /home/core/*.sh; /home/core/operations-in-coco.sh $PREFER_ARCHIVE"
}

backupFromUcs
copyScriptsAndDumpToCoco
runOperationsInCoco