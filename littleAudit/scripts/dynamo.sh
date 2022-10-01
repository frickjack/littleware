#!/bin/bash

AWS_OPTIONS=(--endpoint-url http://localhost:8000  --profile local)
SCRIPT_FOLDER="$(cd "$(dirname "$0")" && pwd)"


arun() {
  aws "${AWS_OPTIONS[@]}" "$@"
}

create() {
  (
    cd "$SCRIPT_FOLDER" \
    && arun dynamodb create-table --cli-input-json "$(cat dynamoTable.json)" \
    && arun dynamodb update-time-to-live --cli-input-json "$(cat dynamoTableTtl.json)"
  )
}

help() {
    cat - <<EOM
Use: dynamo sub-command options
- arun - run aws cli with the given options plus localhost endpoint
- create - create the pubsub dynamodb table
- help
EOM
}

if [[ $# -lt 1 ]]; then
  help
  exit 0
fi

"$@"