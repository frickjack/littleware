# TL;DR

How to dev-test

## Toolchain

```
$ java --version
openjdk 14.0.2 2020-07-14

$ scala --version
Scala code runner version 2.13.4 -- Copyright 2002-2020, LAMP/EPFL and Lightbend, Inc.

$ gradle -v

-----------------------------
Gradle 6.8.1

``` 

### Gradle Cheat Sheet

* `gradle -i` - INFO level [logging](https://docs.gradle.org/current/userguide/logging.html)
* `gradle cleanTest test --tests 'GetOpt*'` - run specific [tests](https://stackoverflow.com/questions/22505533/how-to-run-only-one-unit-test-class-using-gradle)

## JSON structured logs

```
cat $XDG_RUNTIME_DIR/log.ndjson | jq -r 'select(.message[0:1] == "{") | .little_info = (.message | fromjson)'
```

## CICD

## littleAudit

```
#
# Bash function to generate new ES256 key pair
#
newkey() {
    local kid=${1:-$(date +%Y%m)}
    local secretsFolder=$HOME/Secrets/littleAudit
    
    (
        mkdir -p "$secretsFolder"
        cd "$secretsFolder" || return 1
        if [[ ! -f ec256-key-${kid}.pem ]]; then
          openssl ecparam -genkey -name prime256v1 -noout -out ec256-key-${kid}.pem
        fi
        # convert the key to pkcs8 format
        openssl pkcs8 -topk8 -nocrypt -in ec256-key-${kid}.pem -out ec256-pkcs8-key-${kid}.pem
        # extract the public key
        openssl ec -in ec256-pkcs8-key-${kid}.pem -pubout -out ec256-pubkey-${kid}.pem
    )
}


repl() {
    local replPath
    replPath="$(gradle :littleAudit:printClasspath --quiet)" || return 1
    scala -classpath "$replPath"
}

```

## Docker lambda

```
docker build -t 'audit:frickjack' .
docker run -it --name audit --rm -p 9000:8080 audit:frickjack
curl -XPOST "http://localhost:9000/2015-03-31/functions/function/invocations" -d '{}'
```
