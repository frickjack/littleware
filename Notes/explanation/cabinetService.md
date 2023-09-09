## Overview

The little cabinet service implements a simple hierarchical metadata store
for storing and retrieving simple application state.

- A project may have up to 10000 cabinets.
- A cabinet may have up to 10000 drawers.
- A drawer may have up to 10000 folders.
- A folder may have up to 10000 documents.
- A cabinet is just a hierarchy of documents up to 4 levels deep (cabinet, drawer, folder, document)
- Each document may have up to a 10KB json details blob
- Each document may have up to a 100KB json payload
- Each document payload references a schema, but the API does not enforce compliance with the schema
- A blob may be associated with a particular document.
- A document may be locked to support simple distributed transactions managed by the client.  A lock has a ttl and up to 10KB of details metadata.  A locked resource should not be updated until its lock expires or it is unlocked, but the server side does not enforce that constraint - client tools should respect lock semantics (we don't want to get into the game of trying to decide who can lock or unlock documents with different kinds of lock types, etc).  Note that blob upload is an asynchronous process that acquires a lock on its document, and tracks its own blob-information in the document's metadata.
- A document supports simple version-based locking allowing strategies for 
mutual exclusion, counters, and leader election
- A cabinet is not a search index.  The structure of the cabinet supports simple retrieval and scanning given a known organization of documents - like a library.
- A document is not globally accessible by a global id - the retriever must know the folder where a particular document resides.
- An application may implement a strategy where it stores a document according to a global-id scheme, and stores link-documents in other folders, but access to the referenced document is determined by client's ability to access the document's folder.
- A document supports a simple TTL where it self-deletes after the TTL expires
- A version number is associated with the document that increases after each update to the document. The document-update API allows for a simple version-based consistency check, so a client can avoid updating a document that is not in an expected state.
- A document may not be renamed
- A document with children (at the cabinet, drawer, or folder level) may not be marked for delete (non-zero ttl)
- A document may not have more than 10000 children

### Transactions and Higher Level Operations

The server-side security and transaction model is very simple.
Other operations like move and archive are accomplished like this by a client:
* lock the resource
* copy the resource - its blobs, etc - to some destination
* remove (expire) the source resource

The lock accepts a blob of metadata to support client-managed transactions via the saga pattern or whatever.

We may introduce an asynchronous workflow service in the future (leveraging AWS workflow or temporal or conductor or something like that) to implement some standard flows.

### Event Sourcing

In the future, we may extend the cabinet service to publish events to a client supplied event bridge or similar destination.

### Security Model

The core `CabinetService` implementation does not implement its own security model.
Security is implemented by a separate `ProjectCabinetService` that exposes a REST
interface that interacts with the standard littleware permissions and role-based security model.
Checkout the `GH-59/chore/horn_clauses` branch.
So the structure of the code is something like this:

* little security service uses internal `CabinetService` to save the project's security state
* little cabinet service is secured by the little security service, and exposes the `CabinetService` for external consumption
