#!/bin/bash

JSONDOC="publish.json"
BASE_API_URL['Dynpub']="https://dynpub-uk-up.ft.com/lists"
BASE_API_URL['Pre_prod']="https://pre-prod-up.ft.com/lists"
BASE_API_URL['Prod_uk']="https://prod-uk-up.ft.com/lists"
BASE_API_URL['Prod_us']="https://prod-us-up.ft.com/lists"


echo "Environment: ${ENVIRONMENT}"
echo "Base API URL: ${BASE_API_URL[${ENVIRONMENT}]}"
echo "List ID: ${LIST_ID}"
echo "Content IDs: ${CONTENT_IDS}"
echo "Layout Hint: ${LAYOUT_HINT}"
echo "Title: ${TITLE}"
echo "Transaction ID: ${TRANSACTION_ID}"

if [ "${ENVIRONMENT}" =~ "Prod_" && "${PROD_RELEASE}" == "true" ]; then
  echo "Production environment and PROD_RELEASE selected"
if [ "${ENVIRONMENT}" =~ "Prod_" && "${PROD_RELEASE}" == "false" ]; then
  echo "PROD_RELEASE un-selected"
fi

scripts/lists_publish/jsongen.py -t "${TITLE}" -i "${LIST_ID}" -c "${CONTENT_IDS}" -l "${LAYOUT_HINT}" -x "${TRANSACTION_ID}" > ${JSONDOC}

if [[ "$?" -eq "0" ]]; then
  #curl -m 1 --upload-file ${JSONDOC} ${ENDPOINT}
  echo "Upload disabled"
else
  echo "Failed to generate ${JSONDOC}"
  exit 1
fi