# TL;DR

Littleware provides a multi-tenant environment for delivering metered asynchronous API's.

## Requirements

The littleware design attempts to balance several requirements and design decisions.

### Asynchronous API

Rather than try to maintain a response-time and throughput SLA on a synchronous API we chose to base our infrastructure on asynchronous API's.  
An asynchronous API allows a client to start a session,
then submit one or more requests,
and poll for responses.
We do this for several reasons.  First, most interactive clients are asynchronous anyway with an interactive UI thread and background workers handling data.

Second it's easier to manage the backend of an asynchronous API with flexible latency requirements.
Once a synchronous API latency increases above 50
seconds or so, then we begin hitting hard timeouts
at various layers of our stack.  A synchronous API
is also prone to cascade into a failure mode.  If
the API moves into a slow state - maybe it has passed
some data threshold on a backend database - then
requests tend to pile up and retry and load usually
propagates direclty through to the data store.
An asynchronous API can simply queue up requests,
provide feedback on progress, and avoids the "retry on reload" typical of synchronous implementations.

An asynchronous API gracefully degrades by increasing the latency of individual requests, but maintaining aggregage throughput.  Queue lengths and request latency provide metrics for scaling backend infrastructure or
triggering circuit breakers to provide throttling feedback to clients.
An asynchronous API can also provide feedback messages to a client as its
request progresses through authorization, data retrieval, aggregation,
or whatever stages are relevant for the request.

Although the client interacts with the API with asynchronous message exchange,
each API message is handled synchronously by stream processors on the backend.
We can also imagine an API that actually launches a multi-step workflow for
tasks like delivery logistics or collecting signatures from users.  
Workflow management is outside the scope of what we are trying to do here.

### Decoupled Authorization

Authorization should be decoupled from implementation.
In other words - the code that decides if a request
is authorized to execute should be separate from the 
code that carries out the request - a separate service
managed by a separate team.  Something like an
authorizing proxy.  Authorization can include things
like quota enforcement, inspection defenses, and validity checks.

### Multi-tenant and cell based

We want to support multiple tenants on a single platform.  Some environments support multiple tenancy by simply 
deploying separate instances of a single tenant software
stack for each client.  That approach does not scale
well operationally.  However we want our system to support
a cell based infrastructure - where groups of tenants
are mapped to different cells, so we can roll out
updates cell by cell without "big bang" production rollouts.

### Metered and Observable

We want to be able to charge a client based on the
client's use of our system, so clients that use the
system lightly pay less than those that generate a heavy
load.

Along the same lines we want to be able to easily report on what users are doing (or has done), and how well the system is performing.

### Scale down and cheap - storage hierarchy

The majority of the software I have worked on had bursty
access patterns - where the number of interactive users varied from zero off business hours to tens of users during business hours.  We want our services to be highly available with acceptable latency for interactive users, but we also want them to be cheap to operate.

Data for inactive project API's should be easy to move to cheap offline
storage, then pull back online.

## Use Cases

### Authorization as a service

### Secrets exchange

### Simple distributed locks and leader election

### API gateway

Handle authentication and authorization for API's provided by an application built on top of littleware.

### User defined services and service interconnect

Mesh?  Event bus?

## Design

### Multi-tenant

We deploy the system to different regions - where a
region corresponds to the region of an underlying
public cloud.  We initially only support aws-us-east-2.

Each region is in turn divided into clouds - where
each cloud is an operational unit.  A user authenticates with the cloud in which she wants to operate.  We initially
only support three clouds in the aws-us-east-2
region: "free", "dev", and "production".
The "free" cloud allows a user to create a single project (see below) with a limited quota by which the user
can experiment with our API's.  The "free" cloud
is cleared on a bi-weekly basis.

The "dev" cloud is an internal environment for dev, test, and quality assurance.

Finally, the production cloud supports project API's with 
a simple quota-reservation billing model.

Each user that authenticates with the system can
create one or more "projects".  The project
provides a billing and authorization boundary
for interacting with littleware API's.

So the concept hierarchy is:
* region
* cloud - authentication boundary
* project - authorization boundary
* api

Operationally each cloud api is implemented by
services running in one or more cell.
A user begins interacting with an API by
creating a session jwt that authenticates the
api access for a period of time.  The session
jwt specifies the `(user, api, project, cell, ttl)` tuple -
where the cell is endpoint to which the client
posts requests.

### Asynchronous, decoupled authorization, observable

Littleware implements its API's via a progression
of streams - probably implemented as kafka topics.

* the client acquires a session token for that authorizes access to a project's resources via an API.  The session token maps to a particular cell implementing the api.
* the client posts a request message to the API cell's http endpoint using the session token as authorization
* the http proxy appends the request message to a "request" stream partitioned by the session id 
* we currently plan to manage a cell's streams with kafka
* the various stream processors may persist data to
the API's cassandra cluster
* an authorizing stream processor accepts or denies each message in the request stream with an authorization and quota check, and either forwards the message on to a "command" stream or pushes a "message refused" event to a "response" stream
* an api stream processor responds to each event in the "command" stream, and issues a response to the response stream.
* a response processor harvests each response message into a cassandra table (with a ttl)
* the client polls the cell's http proxy to retrieve responses
* an auditing stream processor saves the request, command, and response streams for each project out to S3 in batches; or we may forward events to an archiver api that implements billing, monitoring, and reporting services for each project

## Core API's

### Cloud Authentication

Each cloud implements an authentication mechanism
leveraging cognito federation.
A large customer that wants to integrate with their
own identity provider may either federate that idp
into an existing cloud, or pay to deploy a dedicated cloud.

### Cloud Manager

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

### IAM

The IAM service allows a project user to create robot accounts, user groups, access policies, and link access policies to users, groups, and robots.

A user may download credentials for a robot account.


## lambda eventbus vs ec2 kafka

* low code / no code


## Roadmap

### core services

This is a heavy lift.  Includes the need to figure out how to authorize robot accounts, etc.

### secrets exchange

A simple service for exchanging secrets.

### locks and leaders

### frickjack dev cloud

### free.aws-us-east-2 cloud

Marketing and user feedback.

### prod1.aws-us-east-2 cloud

Way to accept money

### api gateway

### idea - cloud operator

Different organizations can operate clouds geared toward different clients (gov, enterprices, education, research, ...).

## RBAC and ABAC

### Resource specification

Little resource name URI:
```
"lrn://${cloud}/${api}/${project}/${resourceType}/${resourcePath}?tag1=${userTag1}&tag2=${userTag2}"
```

## Roadmap

### Session token vending on api.frickjack.com

* hard coded project and cell for now
* lambda authorizer?
* Docker session token vendor lambda

### api-gateway/event-bus bridge

* Automation for API gateway to lambda to event to request SQS
* Automation for event to response SQS to lambda to dynamo or whatever
* Automation for API gateway to lambada to response

### cell0.frickjack.com infrastructure

* initial cell automation - api gateway, event broker, request queue


## Notes and References

* deploy as few tables as possible to a cassandra cluster: https://thelastpickle.com/blog/2020/11/25/impacts-of-many-tables-on-cassandra.html
* dynamodb design advocates few tables: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-general-nosql-design.html
S
* ABAC - https://en.wikipedia.org/wiki/Attribute-based_access_control
* Claim based access control - https://www.microsoft.com/en-us/download/details.aspx?id=28362
