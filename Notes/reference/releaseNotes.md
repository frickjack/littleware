# TL;DR

Some basic information on our tagged releases.
Note - `git log tag1...tag2` shows the commit log between versions.

## 3.0.1

Build changes.

### Improvements

* SessionLambda sets `Cache-Control` header to `no-store`
* scala3
* sbt build

## 3.0.0

The codebuild process builds this docker image from littleAudit/Dockerfile

```
docker pull 027326493842.dkr.ecr.us-east-2.amazonaws.com/little/session_mgr:3.0.0
```

Initial build of the session manager under the cloud manager module.
Refer to the explanatory docs for an overview of the role the session
manager plays in the littleware cloud architecture.

### Features

* AWS KMS implementation of SessionMgr interface
* JsonConfigLoader and session manager configuration integration
* basic JWT session management with user provisioned keys
* lambda api gateway SessionLambda handler
* Dockerfile wired for AWS lambda integration

### Improvements


### Fixes


### Notes


## 2.6.1

The codebuild process builds this docker image from littleAudit/Dockerfile

```
docker pull 027326493842.dkr.ecr.us-east-2.amazonaws.com/little/session_mgr:3.0.0
```

This is an intermediary release on the way to the 3.0.0 release
with a stub lambda docker for publishing
a tagged docker image to bootstrap our infrastructure automation.
Do not use this tag in production.
