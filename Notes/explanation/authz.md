# TL;DR

Access based access control (ABAC) is a generalization of
role based access control (RBAC) that authorizes based on
the values of attributes associated with the actor,
the action the actor is attempting to perform, the
resource the action accesses or modifies, and the context
in which the request is taking place (time, 
authorization status, client profile, etc).

## Design details

An IAM administrator submits a series of policy documents
that define the rules and assign attributes.
The rules govern whether a actor may take
an action on a resource within a context.
The attributes are assigned to actors, actions, and resources
to augment the attributes dynamically assigned to those entities
at runtime.

### Attributes

An attribute is a key-value pair assigned to an entity, where the value may be a set of strings or a scalar.  For example - to indicate that a actor is a member project team X, and administrator may assign a tag like: `project = [X]`.  A `project_admin = [X]` tag could indicate that the actor is an administrator for project X.

Similarly, we might assign attributes to resources
based on a tree structure:

```
Resource: /tree/objects/${actorid}/*
```


### Service Definition

```
{
    "name": "api1",
    "type": "ServiceDefinition",
    "schema": "...",
    "service": "api1",
    "actions": [
        {
            "name": "api1:get",
            "attrs": {
                "readonly": true
            }
        }, ...
    ]
}
```

### Resource Policy

Resource attributes defined in `ResourcePolicy` documents are merged with resource attributes added to the context for the actor request.

```

{
    "name": "Proj1Resources",
    "type": "ResourcePolicy",
    "service": "api1",
    "schema": "...",
    "resources": [
        {
            "select" : {
                "glob_match": [
                    { "resource_attribute": "lrn" },
                    { "string": "lrn://*/*/*/api1/proj1/**/*"}
                ]
            },
            "statements": [
                { "add": {"projects": "proj1"}}
            ]
        }
    ]
}

```


### Actor Policy

Actor attributes defined in `ActorPolicy` documents are merged with actor attributes added to the context for the actor request.


```
{
    "name": "OperatorGroup",
    "type": "ActorPolicy",
    "schema": "...",
    "actors": [
        {
            "select" : {
                "equal": {
                    { "actor_attribute": "domain" }
                    { "string": "frickjack.com" }
                }
            },
            "statements": [
                { "assign": {"admin": true}}
            ]
        }
    ]
}
```

```
{
    "name": "Proj1Groups",
    "type": "ActorPolicy",
    "schema": "...",
    "actors": [
        {
            "select" : {
                "set_member": [
                    { "actor_attribute": "login" },
                    { "set": [ "reuben@whatever.com", "sharlene@gmail.com" ]}
                ]
            },
            "statements": [
                { "add": { "proejects": "proj1" }}
            ]
        },
        {
            "select" : {
                "set_member": [
                    { "actor_attribute": "login" },
                    { "set": [ "sharlene@gmail.com" ]}
                ]
            },
            "statements": [
                { "add": { "project_admin": "proj1" }}
            ]
        }
    ]
}
```

### Access Policy

```
{
    "name": "Api1DenyRules",
    "type": "AccessPolicy",
    "schema": "...",
    "target": {
        "service": "api1",
        "filter": { "not_empty": { "set": [
            { "actor_attribute": "lastday" },
            { "actor_attribute": "bill_overdue" },
            { "actor_attribute": "black_list" }
        ]}}
    },
    "ruleselect": "firstMatch",
    "rules": [
        {
            "effect": "deny",
            "conditions": { "or": [
                { "date_after": [
                    { "context_attribute": "now" }
                    { "actor_attribute": "lastday" }
                    ]},
                { "equal": [ { "actor_attribute": "black_list" }, { "boolean": true }]},
                { "equal": [ { "actor_attribute": "bill_overdue" }, { "boolean": true }]}
            ]}
        }
    ]
}
```

```
{
    "name": "Api1AdminAccess",
    "type": "AccessPolicy",
    "schema": "...",
    "target": {
        "service": "api1",
        "filter": { "equal": [
            { "actor_attribute": "admin" },
            { "boolean": true }
        ]}
    },
    "ruleselect": "firstMatch",
    "rules": [
        {
            "effect": "allow",
            "conditions": { "boolean": true }
        }
    ]
}
```

```
{
    "name": "Api1ProjectAccess",
    "type": "AccessPolicy",
    "schema": "...",
    "target": {
        "service": "api1",
        "filter": {
            "and": [
                { "or": [ 
                    { "equal": [ { "string": "user" }, 
                                { "actor_attribute": "type" }]},
                    { "equal": [ { "string": "user" }, 
                                { "actor_attribute": "type" }]}
                    ]
                },
                { "equal": [ { "boolean": true }, 
                                { "action_attribute": "readwrite" }]}

        ]
    },
    "ruleselect": "firstMatch",
    "rules": [
        {
            "effect": "allow",
            "conditions": { "and": [
                { "not_empty": { 
                    "set_intersect": [ 
                        { "resource_initial_attribute", "projects" },
                        { "actor_attribute": "projects" } 
                        ]
                    }
                },
                { "not_empty": { 
                    "set_intersect": [ 
                        { "resource_ending_attribute", "projects" },
                        { "actor_attribute": "projects" } 
                        ]
                    }
                }
            ]}
        }
    ]
}
```

```
{
    "name": "Api1AllReadAccess",
    "type": "AccessPolicy",
    "schema": "...",
    "target": {
        "service": "api1",
        "filter": { "boolean": true }
    },
    "ruleselect": "firstMatch",
    "rules": [
        {
            "effect": "allow",
            "conditions": null
        }
    ]
}
```

```
{
    "name": "Api1RequireTags",
    "type": "AccessPolicy",
    "schema": "...",
    "target": {
        "service": "api1",
        "filter": {
            { "equal": [
                { "action_attribute": "readwrite" }
                { "boolean": true }
            ]}
        } 
    },
    "ruleselect": "firstMatch",
    "rules": [
        {
            "effect": "deny",
            "conditions": { "empty": { "resource_ending_attribute": "project" } }
        }
    ]
}

```

## Access Rules

Authz policies are translated into runtime rules that are
evaluated at API access time.

```
trait RuleEffect {}

case object Allow extends RuleEffect {}
case object Deny extends RuleEffect {}

object RuleEffect {
    def fromString(...)
}

case class AccessRule (
    policyLrn: String,
    ruleNumber: Int,
    api: String,
    effect: String,
    decisionTree: DecisionTree
) {

}
```

## Policy Workflows and Access Patterns

### Get documents

* get policies by project, type, and name
* get policies by project, type, and api

### Get rules

* get rules by project and api

## Roadmap

* v1 - null authz - project-owner based api access


## Reference

* ABAC - https://en.wikipedia.org/wiki/Attribute-based_access_control
* Claim based access control - https://www.microsoft.com/en-us/download/details.aspx?id=28362
