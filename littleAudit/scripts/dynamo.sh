#!/bin/bash

AWS_OPTIONS=(--endpoint-url http://localhost:8000  --profile local --no-cli-pager)
SCRIPT_FOLDER="$(cd "$(dirname "$0")" && pwd)"


arun() {
  aws "${AWS_OPTIONS[@]}" "$@"
}

local-run() {
  export AWS_PROFILE=local
  "$@"
}

create-table() {
  (
    cd "$SCRIPT_FOLDER" \
      && arun dynamodb create-table --cli-input-json "$(cat dynamoTable.json)" \
      && arun dynamodb update-time-to-live --cli-input-json "$(cat dynamoTableTtl.json)"
  )
}

start-dynamodb-local() {
  (
    cd "$SCRIPT_FOLDER/../testStuff/dynamodb"
    docker-compose down
    docker-compose pull
    docker-compose up -d
  )
}

env() {
  cat - <<EOM
dynamo() {
  "$SCRIPT_FOLDER/dynamo.sh" \$@
}
EOM
}

help() {
    cat - <<EOM
Helper for initializing the scratch-cabinet table instance in dynamodb-local
Use: dynamo sub-command options
- start-dynamodb-local - launch dynamodb-local
- arun - run aws cli with the given options plus localhost endpoint
- create-table - create the pubsub dynamodb table
- local-run - run the given command with AWS_PROFILE=local environemnt
    ex: ./dynamo.sh local-run sbt
- env - cat the bash commands to alias dynamo to dynamo.sh in current shell
    ex: eval "$(./dynamo.sh env)"
- help

Note: local ~/.aws/config has:

[profile local]
region = us-east-2
output = json

and ~/.aws/crednetials

[local]
aws_secret_access_key = whatever
aws_access_key_id = whatever

EOM
}

if [[ $# -lt 1 ]]; then
  help
  exit 0
fi

"$@"
