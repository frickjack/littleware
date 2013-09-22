littleware-fishRunner
================

glassfish (http://glassfish.java.net/) based web archive (.war) runner -
run with:
fishRunner key value key value ...
Options pulled first from system environment,
then overriden by command line values:
S3_KEY
S3_SECRET
S3_CREDSFILE  - either both S3_KEY and S3_SECRET or S3_CREDSFILE must be defined
WAR_URI - required - either an s3:// URI otherwise treated as local file path
LOGIN_URI - optional - JAAS login.conf location either and s3:// URI otherwise treated as local file path
CONTEXT_ROOT - required - glassfish deploy context root for war
DATABASE_URL - required - ex: postgres://user:password@host:port/database


