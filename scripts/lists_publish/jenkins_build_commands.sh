#!/bin/bash

JSONDOC="publish.json"
CREDENTIALS="${CREDENTIALS-/home/jenkins/coco.txt}"
CONNECT_TIMEOUT="3"
TEST="${TEST-false}"

declare -A BASE_API_URL
BASE_API_URL['Dynpub']="https://dynpub-uk-up.ft.com/__document-store-api/lists"
BASE_API_URL['Pre_prod']="https://pre-prod-up.ft.com/__document-store-api/lists"
BASE_API_URL['Prod_uk']="https://prod-uk-up.ft.com/__document-store-api/lists"
BASE_API_URL['Prod_us']="https://prod-us-up.ft.com/__document-store-api/lists"

function getKeyValueFromFile () {
  # Looks up value for key in format of key = value or key : value. 
  # Separator can be specified as an argument
  # Expects file to contain key only once
  #
  # arg1 = file name to look up
  # arg2 = key to look up
  # arg3 = key-value separator. Default is ' = '
  #
  # USAGE
  # keyval=$(getKeyValueFromFile "${HOME}/.aws/credentials" "aws_access_key_id" "=")
  # echo "Value retuned $keyval"

  if [[ -z "$3" ]]; then
    delimiter=" = "
  else
    delimiter="$3"
  fi
  key=$2

  if [[ -f "$1" ]]; then
    value=$(sed -n "s/^${key}${delimiter}//p" $1)
    rtncode="$?"
    if [[ "$rtncode" -eq "0" ]]; then      
      echo $value
    fi
  fi
}

echo "Environment: ${ENVIRONMENT}"
echo "Base API URL: ${BASE_API_URL[${ENVIRONMENT}]}"
echo "List ID: ${LIST_ID}"
echo "Content IDs: ${CONTENT_IDS}"
echo "Layout Hint: ${LAYOUT_HINT}"
echo "Title: ${TITLE}"
echo "Transaction ID: ${TRANSACTION_ID}"

if [[ "${ENVIRONMENT}" =~ "Prod_" && "${PROD_RELEASE}" == "true" ]]; then
  echo "Production environment and PROD_RELEASE selected"
elif [[ "${ENVIRONMENT}" =~ "Prod_" && "${PROD_RELEASE}" == "false" ]]; then
  echo "PROD_RELEASE must be ticked in order to deploy to ${ENVIRONMENT}"
  exit 1
fi

scripts/lists_publish/jsongen.py -t "${TITLE}" -i "${LIST_ID}" -c "${CONTENT_IDS}" -l "${LAYOUT_HINT}" -x "${TRANSACTION_ID}" -f ${JSONDOC}

coco_credentials=$(getKeyValueFromFile "${CREDENTIALS}" "${ENVIRONMENT}")
if [[ -n ${coco_credentials} ]]; then
    CURL_PARAMS="--user \"${coco_credentials}\" --header \"X-Request-Id: ${TRANSACTION_ID}\" --header \"Content-Type: application/json\" -m ${CONNECT_TIMEOUT} --upload-file ${JSONDOC} ${BASE_API_URL[${ENVIRONMENT}]}/${LIST_ID}"
    if [[ "${TEST}" == "false" ]]; then
      curl "${CURL_PARAMS}"
    else
      echo 'Upload disabled, variable TEST="true"'
      echo "Curl parameters ${CURL_PARAMS}"
    fi
else
    echo "Failed to lookup credentials. Exit 1."
    exit 1
fi

