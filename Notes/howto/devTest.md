# TL;DR

How to dev-test

## Repository Management

[Angular style](https://medium.com/@menuka/writing-meaningful-git-commit-messages-a62756b65c81) commit messages.

[Gitflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) branch management.  A short-lived branches should rebase (rather than merge) to sync up with the long-lived parent it branches off of.

## Toolchain

```
# aws lambda currently has native runtime support for jdk11
$ java --version
openjdk 11.0.10 2021-01-19
OpenJDK Runtime Environment (build 11.0.10+9-Ubuntu-0ubuntu1.20.04)
OpenJDK 64-Bit Server VM (build 11.0.10+9-Ubuntu-0ubuntu1.20.04, mixed mode, sharing)

$ scala --version
Scala code runner version 2.13.4 -- Copyright 2002-2020, LAMP/EPFL and Lightbend, Inc.

$ gradle -v

-----------------------------
Gradle 6.8.1

``` 

### Gradle Cheat Sheet

* `gradle -i` - INFO level [logging](https://docs.gradle.org/current/userguide/logging.html)
* `gradle cleanTest test --tests 'GetOpt*'` - run specific [tests](https://stackoverflow.com/questions/22505533/how-to-run-only-one-unit-test-class-using-gradle)

## Audit dependencies

We use the [OWASP gradle plugin](https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/configuration.html) to check our dependencies against
public vulnerability databases.

```
(cd littleAudit && gradle dependencyAnalyze)
```

## JSON structured logs

We use log4j to setup json structured logs.  Ex:

```
littleware$ cat littleAudit/src/main/resources/log4j2.xml 
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="DEBUG">
    <Appenders>
        <Console name="LogToConsole" target="SYSTEM_OUT">
            <JsonTemplateLayout eventTemplateUri="classpath:LogstashJsonEventLayoutV1.json"/>
        </Console>
    </Appenders>
    <Loggers>
        <!-- avoid duplicated logs with additivity=false 
        <Logger name="com.mkyong" level="debug" additivity="false">
            <AppenderRef ref="LogToConsole"/>
        </Logger>
        -->
        <Root level="info">
            <AppenderRef ref="LogToConsole"/>
        </Root>
    </Loggers>
</Configuration>
```

We can analyze a log stream like this:
```
cat $XDG_RUNTIME_DIR/log.ndjson | jq -r 'select(.message[0:1] == "{") | .little_info = (.message | fromjson)'
```

## CICD

## littleAudit cloudmgr

Some bash helper functions.
Modify to suit your environment, 
and source these into your dev shell.

```
#
# Bash function to generate new ES256 local key pair
#
newkey() {
    local kid=${1:-$(date +%Y%m)}
    local secretsFolder=$HOME/Secrets/littleware/cloudmgr
    
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

The cloudmgr test suite accesses AWS KMS, so must be run
with AWS credentials.  The `little` command (from https://github.com/frickjack/misc-stuff) will do that for you for local testing:
```
little gradle build
```
Otherwise you can do something like this:
```
(
    export AWS_REGION=us-east-2
    export AWS_SECRET_ACCESS_KEY=...
    export AWS_ACCESS_KEY_ID=...
    cd littleAudit
    gradle build
)
```

## Docker lambda

```
(
cd littleAudit
little gradle build
docker build -t 'audit:frickjack' .
docker run -it --name audit --rm -p 9000:8080 audit:frickjack
curl -XPOST "http://localhost:9000/2015-03-31/functions/function/invocations" -d '{ "httpMethod": "OPTIONS" }'
)
```

## Configuration

The `little.cloudmgr.domain` and `little.cloudmgr.sessionmgr.config`
configuration keys may be set in either `littleware.properties` or
the `LITTLE_CLOUDMGR` environment variable.

```
$ cat littleAudit/src/test/resources/littleware.properties 
little.cloudmgr.domain = test-cloud.frickjack.com
little.cloudmgr.sessionmgr.config = { \
    "configSource": "this", \
    "sessionMgr": "local", \
    "localSessionConfig": { \
        "signingKey": { "kid": "testkey", "pem": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----" }, \
        "verifyKeys": [ \
            { "kid": "testkey", "pem": "-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----" } \
        ], \
        "oidcJwksUrl": "https://www.googleapis.com/oauth2/v3/certs" \
    } \
}
# ...
```

The `jwks.json` enpoint for a cognito deployment is in its openid configuration - ex: https://cognito-idp.us-east-2.amazonaws.com/us-east-2_860PcgyKN/.well-known/openid-configuration


## Publish by git tag

(
  version="$(gradle --quiet :littleAudit:printVersion)"
  git tag -a "$version" -m "release details in Notes/reference/releaseNotes.md#$version"
  git push origin $version
)
