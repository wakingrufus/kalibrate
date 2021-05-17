# Kalibrate
A load testing framework for Kotlin

### Session
This class will hold any runtime parameters you want to pass, as well as any runtime variables you need to store (ie auth tokens).
Create a simple data class to hold these. It can be named anything.
```kotlin
data class Session(
    val scenario: String = "deploy",
    val test: String = "start",
    val baseUrl: String = ""
)

```

### DSL
Your Kalibrate application will use a DSL starting your applicatino's main method:

```kotlin
@FlowPreview
@KtorExperimentalAPI
fun main(args: Array<String>) = kalibrate(args, { Session() }) { ... }
```

### Jackson
Within the DSL, you can configure Jackson for serialization:
```kotlin
    jackson {
        registerModule(KotlinModule())
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }
```

### Arguments
To define command-line arguments, you can use ArgParser's DSL:
```kotlin
    sessionArgs {
        val scenario by it.storing("scenario name")
        val baseUrl: String by it.storing(
            "-u", "--url",
            help = "base url to use for requests"
        ).default("https://httpbin.org/get")
        Session(scenario = scenario, baseUrl = baseUrl)
    }
```



### Agents
you define an httpAgent for each type of call you will make:
```kotlin
val get = httpAgent<GetResponse>(url = { "${it.baseUrl}?test=${it.test}" })
val post = httpAgent<PostResponse>(url = { "${it.baseUrl}/postEndpoint" }){
    header { "Authorization" to "Bearer ${it.token.orEmpty()}" }
    body { "sample body" }
}
```

### Global http config
```kotlin
    globalHttpConfig {
        header { "Authorization" to "Bearer ${it.token.orEmpty()}" }
    }
```

### Scenarios
Your application can contain many scenarios. You can define a function to use to determine which one to run for each execution:
```kotlin
scenarioChooser { it.scenario }
```