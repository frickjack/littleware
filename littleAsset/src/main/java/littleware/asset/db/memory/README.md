# TL;DR

In-memory backend database implementation based on
snapshots and update logs.

# Details

## Consistency Model

This backend implements a single-writer model - where
one node manages all updates, and other read-only nodes
may lazily synchronize with the writer via a published update log.
The system maintains a monotonically increasing timestamp,
and assigns each
transaction a timestamp when it begins.
During execution a transaction accumulates a log of asset updates.
At transaction completion time (commit), the backend assigns a timestamp to the transaction, and commits the transaction's 
update log to the database, and the global update log.

The backend may run multiple updates in parallel, but if 
concurrently running transacions update the same asset,
then only the first transaction to finish succeeds, and the
others fail with a consistency exception.  So multiple transacions
may run in parallel as long as they do not update the same
assets.  This model allows us to implement consistent locks and counters at the application level.