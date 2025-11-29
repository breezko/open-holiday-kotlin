# Samples

Run `./gradlew publishToMavenLocal` in the repository root first so both
`open-holiday-kotlin-ktor` and `open-holiday-kotlin-spring` are available to
the sample projects via `mavenLocal()`.

## Ktor sample

```
./gradlew -p samples/ktor-sample run
```

This launches `org.openholidays.KtorSampleKt`, fetches public holidays for
Germany in 2025 via the Ktor client, and prints a subset of the results.

## Spring WebClient sample

```
./gradlew -p samples/spring-sample run
```

This runs `org.openholidays.SpringSampleKt`, exercises the Spring WebClient
implementation against Austrian school holidays, and prints a few entries.
