# TL;DR

How to inject configuration into an app


## Problem and Audience

A developer of a system that uses json web tokens (JWT) to authenticate HTTP API requests needs to generate asymmetric cryptographic keys, load the keys into code, then use the keys to sign and validate tokens.

* configuration discovery
* default config overlay
* bind config strings for injection
* configuration object construction
* interface binding

## Summary

To sign and verify JWTs we need to generate keys, load the keys into code, and use the keys to sign and verify tokens.  We plan to add support for token signing with AWS KMS soon.
