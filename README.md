# TL;DR

A mono-repo with various java and scala projects.

## littleware projects

Active folders:

* [./littleware/](./littleware/) - Stable - Guice based dependency injection, builder pattern support, misc utilities
* [./littleScala/](./littleScala/) - Stable - builder pattern support
* [./littleAudit/](./littleAudit/) - Active Development - little cloud service framework

These folders hold projects that have been suspended or deprecated, but
we have not yet erased out of the repo.

* [./littleAsset/](./littleAsset/) - Suspended/Deprecated - j2ee based asset management
* [./littleTools/](./littleTools/) - Suspended/Deprecated - client tools for littleAsset/
* [./littleWeb/](./littleWeb/) - Suspended/Deprecated - littleAsset/ HTTP API bindings
* [./littleDistro/](./littleDistro/) - Suspended/Deprecated - distribution package for littleAsset stuff
* [./webapp/](./webapp/) - Suspended/Deprecated - holds various subtools including littleId/ (old openId authenticator), fishRunner/ (glassfish based servlet-runner cli), and s3Copy/ (publish web site files to S3 with compression and metadata)

## Other repositories

The littleware code base has branched out to other repositories over the years.

* [misc-stuff](https://github.com/frickjack/misc-stuff) - infrastructure automation
* [little-nodedev](https://github.com/frickjack/little-nodedev) - gulpjs based build tools for typescript and web projects
* [little-elements](https://github.com/frickjack/little-elements) - typescript npm package supporting nodejs and web apps
* [little-authn](https://github.com/frickjack/little-elements) - typescript nodejs OIDC (AWS Cognito) client wired to support expressjs or lambda API gateway deployment
* [little-apps](https://github.com/frickjack/little-apps) - typescript and web resources that define https://apps.frickjack.com

## Notes TOC

The Notes/ folder holds a hierarchy of documentation.  
[This site](https://documentation.divio.com/introduction/) for an 
introduction to the different types of documentation (explanation, how-to, tutorial, reference).


### How-to

* [dev-test](./Notes/howto/devTest.md)

### Explanation

* [little-cloud architecture](./Notes/explanation/littleArchitecture.md) - WIP
* [cloud manager](./Notes/explanation/cloudmgr.md) - WIP
* [authz design](./Notes/explanation/authz.md) - WIP
* [working with session tokens](./Notes/explanation/signingJwts.md)
* [cicd](./Notes/explanation/cicd.md) with [codebuild](https://aws.amazon.com/codebuild)

### Reference

* [release notes](./Notes/reference/releaseNotes.md)
