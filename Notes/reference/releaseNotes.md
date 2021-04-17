# TL;DR

Some basic information on our tagged releases.
Note - `git log tag1...tag2` shows the commit log between versions.

## 3.0.0

The codebuild process builds this docker image from littleAudit/Dockerfile

```
docker pull 027326493842.dkr.ecr.us-east-2.amazonaws.com/little/session_mgr:3.0.0
```

#### Features

* basic JWT session management with user provisioned keys (TODO - kms integration)
* stub lambda api gateway RequestHandler
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
