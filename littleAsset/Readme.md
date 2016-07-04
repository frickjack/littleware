littleware asset module
========================

## Overview

Littleware's Asset module implements a simple tree-based data store.  
The module has pluggable backends, but works best with a consistent data store
like SQL and Redis as opposed to an eventually consistent store like DynamoDB.

The asset module's data model is a simple graph of interconnected nodes, but functions 
best when organized as a tree with where each node has no more than 1000 children.
In exchange for adopting this restrictive data model the API provides
several useful services:

* ownership tracking
* access control
* update auditing
* "Home" assets - data sharding
* scalability 
* simple opinionated pattern path to progressive enhancement

The API does not directly support transactions, but application-level locking
is easily implemented, since updates to an individual node are atomic.


## Ideas 

* Lock server
* Audit log
* RBAC
* User groups
* Simple DAM


## Design Patterns and Examples

* Record subtrees
* Decorator nodes
* Date-based subtrees
