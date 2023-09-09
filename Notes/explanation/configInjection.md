# TL;DR

How to inject configuration into an app

## Problem and Audience

One of the things every microservice needs is a mechanism
for injecting configuration, so we developed a little json configuration helper for our littleware scala code that overlays a hierarchy of json configuration objects, and integrates with our module runtime and dependency-injection framework.


## Configuration in Littleware

Littleware has a simple [ServiceLoader](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/ServiceLoader.html) based module runtime system
that integrates with a [guice](https://github.com/google/guice) dependency injection container.  In practice what that means is that each java or scala jar includes a `Module` class that implements a simple callback interface for defining configuration injection bindings, and registering application event listeners (startup and shutdown).  We have now augmented this platform with a json configuration helper that allows a module developer to provide configuration defaults on the classpath in the jar file, and the stack operator to override those defaults with a json file on an environment-defined search path or with json in an environment variable.

Here's how it works.  The [JsonConfigLoader](https://github.com/frickjack/littleware/blob/main/littleScala/src/main/scala/littleware/scala/JsonConfigLoader.scala) provides a `loadConfig` helper that takes a `key` as an argument and returns a `JsonObject` (we use the [gson](https://github.com/google/gson) json library).  

The `JsonConfigLoader` also provides a `bindKeys` method that consumes a json object and a guice binder, converts the json to a list of (key, value) pairs, maps the values back to strings, and binds each key to its string value using guice's `@Named` [binding facility](https://github.com/google/guice/wiki/BindingAnnotations#named).

So in the `Module.scala` (or `.java`) file described above, the module bootstrap code does something like [this](https://github.com/frickjack/littleware/blob/main/littleAudit/src/main/scala/littleware/cloudmgr/service/littleModule/AppModuleFactory.scala):
```
littleware.scala.JsonConfigLoader.loadConfig(CONFIG_KEY, getClass().getClassLoader()).map(
  {
    jsConfig =>
    littleware.scala.JsonConfigLoader.bindKeys(binder, jsConfig)
  }
)
```

Finally, a configuration provider can consume the bound configuration strings - like [this](https://github.com/frickjack/littleware/blob/main/littleAudit/src/main/scala/littleware/cloudmgr/service/internal/AwsSessionMgr.scala):
```
@inject.Singleton()
    class ConfigProvider @inject.Inject() (
        @inject.name.Named("little.cloudmgr.sessionmgr.awsconfig") configStr:String,
        gs: gson.Gson
    ) extends inject.Provider[Config] {
        lazy val singleton: Config = {
            val js = gs.fromJson(configStr, classOf[gson.JsonObject])
            Config(
              js.getAsJsonPrimitive("oidcJwksUrl").getAsString(),
              Option(js.getAsJsonPrimitive("kmsSigningKey")).map({ _.getAsString() }),
              js.getAsJsonArray("kmsPublicKeys").asScala.map({ jsIt => jsIt.getAsJsonPrimitive().getAsString() }).toSet
            )
        }

        override def get():Config = singleton
    }

```

In the `cloudmgr` module above the configuration key is `LITTLE_CLOUDMGR`, so the config loader first loads
`littleware/config/LITTLE_CLOUDMGR.json` off the classpath - which provides some developer defaults.  The loader then searches the folders from the `LITTLE_CONFIG_PATH` environment (or system) variable until it finds a `LITTLE_CLOUDMGR.json` file, and it loads that, and does a shallow json merge.  Finally, the config loader looks for a `LITTLE_CLOUDMGR` system (or environment) variable, and again merges the keys.

What does our configuration look like?  We want to avoid collisions between binding keys from different modules, so the keys in a config json follow the java package reverse-dns pattern.  Also, I like to have simple patterns that I can follow, so each service implementation in the module that requires configuration defines its own `Config` class and `Provider[Config]` that consumes a particular configuration key (that can be individually overriden via the configuration merge process described above).  For example, the `cloudmgr` module has two service implementation, `LocalKeySessionMgr` and `AwsSessionMgr`, and the json configuration for the module looks like [this](https://github.com/frickjack/littleware/blob/main/littleAudit/src/main/resources/littleware/config/LITTLE_CLOUDMGR.json):
```
{
    "little.cloudmgr.domain" : "test-cloud.frickjack.com",
    "little.cloudmgr.sessionmgr.type": "local",
    "little.cloudmgr.sessionmgr.localconfig": {
        "signingKey": { "kid": "testkey", "pem": "-----BEGIN PRIVATE KEY-----\nMIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgs02I2exqJsdAoHef\n54/cjmlRvww903MKp0AOPqlRRXqhRANCAATWdeIowEmJ5lxpm7gE8GtvBnB1FBTI\nlcZHdD1FPM90oeEAraGGtnluYYEdPiJP3r29n3qFcGTgvqDAE49bc4om\n-----END PRIVATE KEY-----" }, 
        "verifyKeys": [ 
            { "kid": "testkey", "pem": "-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE1nXiKMBJieZcaZu4BPBrbwZwdRQU\nyJXGR3Q9RTzPdKHhAK2hhrZ5bmGBHT4iT969vZ96hXBk4L6gwBOPW3OKJg==\n-----END PUBLIC KEY-----" } 
        ],
        "oidcJwksUrl": "https://www.googleapis.com/oauth2/v3/certs" 
    }, 
    "little.cloudmgr.sessionmgr.awsconfig": {
        "kmsPublicKeys": [ 
            "alias/littleware/api/api-frickjack-com/sessMgrSigningKey", 
            "alias/littleware/api/api-frickjack-com/sessMgrOldKey" 
        ], 
        "kmsSigningKey": "alias/littleware/api/api-frickjack-com/sessMgrSigningKey", 
        "oidcJwksUrl": "https://cognito-idp.us-east-2.amazonaws.com/us-east-2_860PcgyKN/.well-known/jwks.json"
    },
    "little.cloudmgr.sessionmgr.lambdaconfig": {
        "corsDomainWhiteList": [ ".frickjack.com" ],
        "cookieDomain": ".frickjack.com"
    }
}
```

## Summary

We developed a little json configuration helper for our littleware scala code that overlays a hierarchy of json configuration objects, and integrates with our module runtime and dependency-injection framework.