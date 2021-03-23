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
newkey() {
    local kid=${1:-$(date +%Y%m)}
    local secretsfolder=$HOME/Secrets/littleAudit
    
    (
        cd $secretsFolder
        openssl ecparam -genkey -name prime256v1 -noout -out ec256-key-${kid}.pem
        openssl pkcs8 -topk8 -nocrypt -in ec256-key.pem -out ec256-pkcs8-key-${kid}.pem
        openssl ec -in ec256-key.pem -pubout -out ec256-pubkey-${kid}.pem
    )
}

exportkeys() {
    kid=testkey
    secretsfolder=$HOME/Secrets/littleAudit
    export LITTLE_AUDIT_PUBKEY="$(cat /ec256-pubkey.pem)"
    export LITTLE_AUDIT_PRIVKEY="$(cat $secretsfolder/ec256-pkcs8-key.pem)"
}

```
