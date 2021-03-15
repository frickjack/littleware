# TL;DR

The cloud manager allows a user to create a project, activate an API for that project, and acquire a session
for interacting with an API.

The cloud manager includes internal API's for registering a cell for an API.  The cloud manager
assigns a project to a cell at API activation time.
A new cell should be introduced whenever the load on
an existing cell reaches a level that requires the cell
to scale up and out.  Different API's may each require
a different mix of resources (kafka nodes, cassandra nodes, compute nodes, storage, etc).

Each API activated for a project has a robot account associated with it that gives the API the permissions it
needs to interact with other API's (like IAM).


## User Workflows and Access Patterns

NoSQL schema design requires an understanding of data access patterns.  See the cql table definitions for the [cloudmgr keystore](../../littleAudit/scripts/cloudmgr.cql) and the [authz keystore](../../littleAudit/scripts/authz.cql).


### CRUD project

Request:
```
{
    "event": "requestCreateProject",
    "properties": {
        "name": "dev1.myorg",
        "owners": []
    }
}
```


https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html



Response:
```
{
    "event": "projectUpdated",
    "properties": {
        "lrn": "lrn:${cloud}:cm::aws-us-east-2:event:$eventId",
        "requestId": "...",
        "date": "...",
        "payload": {
            "projid": "XXXXXX...whatever",
            "lrn": "lrn:${cloud}:cm:proj:aws-us-east-2:${projid}"
            "name": "dev1.myorg",
            "owners": [],
            "robots": [],
            "users": [],
            "apis": [ "authz" ]
        }
    }
}
```

* get projects by name - `cm:getProjects`
* get projects for owner - `cm:getProjects`
* get projects owned by me - `cm:getMyProjects`

### CRUD cloud operators

* get operators - `cm:GetOperators`
* get operators by api - `cm:GetOperators`

### Cell CRUD API-Project

Internal API to manage a cell's project data

* enable (project, api) - `cm:enableProject`
* archive (project, api) - `cm:archiveProject`
* restore (project, api) - `cm:restoreProject`

* get cells for api - `cm:getCellsForApi`
* get cells for project - `cm:getCellsForProject`


#### Create Session

Session table may have a TTL.

* new session(project, api) -> (project, api, cell, user)
* get sessions for project, time-range
* get sessions for user, time-range


### Authz policy interactions

#### CRUD policies
