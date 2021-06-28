# TL;DR

Our codebuild [buildspec](../../buildspec.yml) is pretty straight forward.

## Problem and Audience

A continuous integration (CI) process that builds and tests our code,
then publishes versioned deployable artifacts (docker images) 
is a prerequisite for deploying stable software services in the cloud.
There are a wide variety of good, inexpensive CI services available,
but we decided to build littleware's CI system on [AWS codebuild](https://aws.amazon.com/codebuild), because it provides an easy to use serverless solution that supports the technology we build on (nodejs, java, scala, docker), and integrates well with AWS.
It was straight forward for us to setup a [codebuild](https://aws.amazon.com/codebuild) CI process ([buildspec.yml](https://github.com/frickjack/little-automation/blob/main/buildspec.yml)) for our little [scala](https://scala-lang.org) project
given the [tools](https://github.com/frickjack/little-automation/blob/main/AWS/doc/stack.md) we already have in place to deploy cloudformation stacks that define the codebuild project and [ecr](https://aws.amazon.com/ecr) docker repository.

## Overview

There were two steps to setting up our CI build: create the infrastructure, then debug and deploy the build script.  The first step was easy, since we already have cloudformation templates for [codebuild projects](https://github.com/frickjack/little-automation/blob/main/AWS/lib/cloudformation/cicd/nodeBuild.json) and [ecr repositories](https://github.com/frickjack/little-automation/blob/main/AWS/lib/cloudformation/cloud/ecr/ecr.json) that our [little stack](https://github.com/frickjack/little-automation/blob/main/AWS/doc/stack.md) tool can deploy.  For example, we deployed the
codebuild project to build the [littlware github repo](https://github.com/frickjack/littleware) by running:
```
little stack create ./stackParams.json
```
with [this](https://github.com/frickjack/little-automation/blob/main/AWS/db/cloudformation/frickjack/cloud/ecr/stackParams.json) parameters file (stackParams.json):
```
{
    "StackName": "build-littleware",
    "Capabilities": [
        "CAPABILITY_NAMED_IAM"
    ],
    "TimeoutInMinutes": 10,
    "EnableTerminationProtection": true,
    "Parameters" : [
        {
            "ParameterKey": "PrivilegedMode",
            "ParameterValue": "true"
        },
        {
            "ParameterKey": "ProjectName",
            "ParameterValue": "cicd-littleware"
        },
        {
            "ParameterKey": "ServiceRole",
            "ParameterValue": "arn:aws:iam::027326493842:role/littleCodeBuild"
        },
        {
            "ParameterKey": "GithubRepo",
            "ParameterValue": "https://github.com/frickjack/littleware.git"
        }
    ],
    "Tags": [
            {
                "Key": "org",
                "Value": "applications"
            },
            {
                "Key": "project",
                "Value": "cicd-littleware"
            },
            {
                "Key": "stack",
                "Value": "frickjack.com"
            },
            {
                "Key": "stage",
                "Value": "dev"
            },
            {
              "Key": "role",
              "Value": "build"
            }
    ],
    "Littleware": {
        "TemplatePath": "lib/cloudformation/cicd/nodeBuild.json"
    }
}

```

With our infrastructure in place, we can add our [build script](https://github.com/frickjack/littleware/blob/main/buildspec.yml) to our github repository.
There a few things to notice about our build script.  First, the littleware git repo holds multiple interrelated projects - java and scala libraries and 
applications that build on top of them.  We are currently interested in building and packaging the `littleAudit/` folder (that will probably be renamed),
so the build begins by moving to that folder:
```
  build:
    commands:
      - cd littleAudit

```

Next, we setup
our codebuild project to run the build container in [privileged mode](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-codebuild-project-environment.html#cfn-codebuild-project-environment-privilegedmode),
so our build can start a docker daemon, and build docker images:
```
phases:
  install:
    runtime-versions:
      # see https://github.com/aws/aws-codebuild-docker-images/blob/main/ubuntu/standard/5.0/Dockerfile
      java: corretto11
    commands:
      # see https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-codebuild-project-environment.html#cfn-codebuild-project-environment-privilegedmode
      - nohup /usr/local/bin/dockerd --host=unix:///var/run/docker.sock --host=tcp://0.0.0.0:2375 --storage-driver=overlay &
      - timeout 15 sh -c "until docker info; do echo .; sleep 1; done"
```

We use [gradle](https://gradle.org) to compile our code and run the unit test suite.  The [org.owasp.dependencycheck gradle plugin](https://plugins.gradle.org/plugin/org.owasp.dependencycheck) adds a `dependencyCheckAnalyze` task that checks our maven dependencies against public databases of known vulnerabilities:
```
  build:
    commands:
      - cd littleAudit
      - gradle build
      - gradle dependencyCheckAnalyze
      - docker build -t codebuild:frickjack .
```

Finally, our post-build command tags and pushes the docker image to
an [ecr](https://aws.amazon.com/ecr) repository.  The tagging rules
align with the lifecycle rules on the repository 
(described [here](https://github.com/frickjack/little-automation/blob/main/Notes/explanation/ecrSetup.md) and [here](https://blog.frickjack.com/2021/04/setup-ecr-on-aws.html)).
```
  post_build:
    commands:
      - BUILD_TYPE="$(echo "$CODEBUILD_WEBHOOK_TRIGGER" | awk -F / '{ print $1 }')"
      - echo "BUILD_TYPE is $BUILD_TYPE"
      - |
        (
          little() {
              bash "$CODEBUILD_SRC_DIR_HELPERS/AWS/little.sh" "$@"
          }

          scanresult=""
          scan_in_progress() {
            local image
            image="$1"
            if ! shift; then
                echo "invalid scan image"
                exit 1
            fi
            local tag
            local repo
            tag="$(echo "$image" | awk -F : '{ print $2 }')"
            repo="$(echo "$image" | awk -F : '{ print $1 }' | cut -d / -f 2-)"
            scanresult="$(little ecr scanreport "$repo" "$tag")"
            test "$(echo "$scanresult" | jq -e -r .imageScanStatus.status)" = IN_PROGRESS
          }

          TAGSUFFIX="$(echo "$CODEBUILD_WEBHOOK_TRIGGER" | awk -F / '{ suff=$2; gsub(/[ @/]+/, "_", suff); print suff }')"
          LITTLE_REPO_NAME=little/session_mgr
          LITTLE_DOCKER_REG="$(little ecr registry)" || exit 1
          LITTLE_DOCKER_REPO="${LITTLE_DOCKER_REG}/${LITTLE_REPO_NAME}"
          
          little ecr login || exit 1
          if test "$BUILD_TYPE" = pr; then
            TAGNAME="${LITTLE_DOCKER_REPO}:gitpr_${TAGSUFFIX}"
            docker tag codebuild:frickjack "$TAGNAME"
            docker push "$TAGNAME"
          elif test "$BUILD_TYPE" = branch; then
            TAGNAME="${LITTLE_DOCKER_REPO}:gitbranch_${TAGSUFFIX}"
            docker tag codebuild:frickjack "$TAGNAME"
            docker push "$TAGNAME"
          elif test "$BUILD_TYPE" = tag \
            && (echo "$TAGSUFFIX" | grep -E '^[0-9]{1,}\.[0-9]{1,}\.[0-9]{1,}$' > /dev/null); then
            # semver tag
            TAGNAME="${LITTLE_DOCKER_REPO}:gitbranch_${TAGSUFFIX}"
            if ! docker tag codebuild:frickjack "$TAGNAME"; then
              echo "ERROR: failed to tag image with $TAGNAME"
              exit 1
            fi
            ...
```
If the CI build was triggered by a semver git tag, then
it waits for the ecr image scan to complete successfully before
tagging the docker image for production use:
```
       ...
          elif test "$BUILD_TYPE" = tag \
            && (echo "$TAGSUFFIX" | grep -E '^[0-9]{1,}\.[0-9]{1,}\.[0-9]{1,}$' > /dev/null); then
            # semver tag
            TAGNAME="${LITTLE_DOCKER_REPO}:gitbranch_${TAGSUFFIX}"
            if ! docker tag codebuild:frickjack "$TAGNAME"; then
              echo "ERROR: failed to tag image with $TAGNAME"
              exit 1
            fi
            # see https://docs.aws.amazon.com/AmazonECR/latest/APIReference/API_ImageScanStatus.html
            docker push "$TAGNAME" || exit 1
            count=0
            sleep 10

            while scan_in_progress "$TAGNAME" && test "$count" -lt 50; do
              echo "Waiting for security scan - sleep 10"
              count=$((count + 1))
              sleep 10
            done
            echo "Got image scan result: $scanresult"
            if ! test "$(echo "$scanresult" | jq -e -r .imageScanStatus.status)" = COMPLETE \
               || ! test "$(echo "$scanresult" | jq -e -r '.imageScanFindingsSummary.findingSeverityCounts.HIGH // 0')" = 0 \
               || ! test "$(echo "$scanresult" | jq -e -r '.imageScanFindingsSummary.findingSeverityCounts.CRITICAL // 0')" = 0; then
               echo "Image $TAGNAME failed security scan - bailing out"
               exit 1
            fi
            SEMVER="${LITTLE_DOCKER_REPO}:${TAGSUFFIX}"
            docker tag "$TAGNAME" "$SEMVER"
            docker push "$SEMVER"
          else
            echo "No docker publish for build: $BUILD_TYPE $TAGSUFFIX"
          fi
```

## Summary

A continuous integration (CI) process that builds and tests our code,
then publishes versioned deployable artifacts (docker images) 
is a prerequisite for deploying stable software services in the cloud.
Our codebuild CI project builds and publishes the docker images that we will
use to deploy our "little session manager" service as a lambda behind an API gateway (but we're still working on that).
