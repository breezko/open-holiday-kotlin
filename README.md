# Open Holiday Kotlin Client

A **framework-agnostic** Kotlin client library for the [OpenHolidays API](https://openholidaysapi.org), providing access to public and school holiday data for countries worldwide.

## Design Philosophy

This library follows a **framework-agnostic** architecture:

- **Core API**: Plain Kotlin interfaces and data classes with no framework dependencies
- **Multiple Implementations**: Built-in support for Ktor and Spring WebClient
- **Easy Integration**: Works with any Kotlin/JVM project, including Spring Boot, Ktor, Quarkus, Micronaut, etc.
- **Extensible**: Create your own implementation with OkHttp, HttpClient, or any HTTP library

## Features

- **Framework-agnostic core** - No forced dependencies
- **Kotlin Coroutines** - All methods are suspend functions
- **Type-safe API** - Leverages Kotlin's type system
- **Multiple implementations** - Choose between Ktor or Spring WebClient
- **Comprehensive coverage** - All OpenHolidays API endpoints supported
- **Well documented** - KDoc comments on all public APIs
- **Localization support** - Query data in multiple languages

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("dev.breezko:open-holiday-kotlin:1.0.1")
    
    // Choose your HTTP client implementation:
    // For Ktor (included by default)
    // No additional dependencies needed - Ktor is bundled
    
    // For Spring WebClient (optional - already included if using Spring Boot with WebFlux)
    implementation("org.springframework:spring-webflux:6.2.0")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0")
}
```

**Note**: The library uses `java.time.LocalDate` from the JDK, so there are no additional date/time dependencies required. This makes it compatible with all JVM frameworks including Spring, Ktor, Micronaut, Quarkus, etc.

## Quick Start

### Using Ktor Implementation

```kotlin
import org.openholidays.HolidaysClient
import org.openholidays.ktor.KtorHolidaysClient
import java.time.LocalDate

val client: HolidaysClient = KtorHolidaysClient.create()

try {
    val holidays = client.getPublicHolidays(
        countryIsoCode = "US",
        validFrom = LocalDate.of(2025, 1, 1),
        validTo = LocalDate.of(2025, 12, 31),
        languageIsoCode = "EN"
    )
    
    holidays.forEach { holiday ->
        println("${holiday.startDate}: ${holiday.name.first().text}")
    }
} finally {
    (client as? AutoCloseable)?.close()
}
```

### Using Spring WebClient Implementation

```kotlin
import org.openholidays.HolidaysClient
import org.openholidays.spring.SpringWebClientHolidaysClient

// Standalone usage
val client: HolidaysClient = SpringWebClientHolidaysClient.create()

// Or with Spring Boot dependency injection:
@Configuration
class HolidaysConfig {
    @Bean
    fun holidaysClient(webClientBuilder: WebClient.Builder): HolidaysClient {
        return SpringWebClientHolidaysClient.create(
            webClientBuilder = webClientBuilder
        )
    }
}

@Service
class HolidayService(private val holidaysClient: HolidaysClient) {
    suspend fun getHolidays(country: String): List<Holiday> {
        return holidaysClient.getPublicHolidays(
            countryIsoCode = country,
            validFrom = LocalDate.of(2025, 1, 1),
            validTo = LocalDate.of(2025, 12, 31)
        )
    }
}
```

## API Overview

The `HolidaysClient` interface provides the following methods:

### Countries & Languages
- `getCountries()` - List all supported countries
- `getLanguages()` - List all supported languages
- `getSubdivisions()` - Get subdivisions (states/provinces) for a country
- `getGroups()` - Get groups (school types) for a country

### Public Holidays
- `getPublicHolidays()` - Get public holidays for a country in a date range
- `getPublicHolidaysByDate()` - Get all public holidays on a specific date
- `getPublicHolidayStatistics()` - Get statistics about public holiday data

### School Holidays
- `getSchoolHolidays()` - Get school holidays for a country in a date range
- `getSchoolHolidaysByDate()` - Get all school holidays on a specific date
- `getSchoolHolidayStatistics()` - Get statistics about school holiday data

## Data Models

All data models are plain Kotlin data classes in the `org.openholidays.model` package:

```kotlin
data class Holiday(
    val id: String,
    val name: List<LocalizedText>,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val type: HolidayType,
    val nationwide: Boolean,
    // ... more fields
)

data class Country(
    val isoCode: String,
    val name: List<LocalizedText>,
    val officialLanguages: List<String>
)
```

## Advanced Usage

### Custom Ktor Configuration

```kotlin
val client = KtorHolidaysClient.create {
    // Custom Ktor configuration
    install(HttpTimeout) {
        requestTimeoutMillis = 30000
    }
}
```

### Error Handling

```kotlin
try {
    val holidays = client.getPublicHolidays(...)
} catch (e: HolidaysApiException) {
    println("API Error: HTTP ${e.statusCode}")
    e.problem?.let { problem ->
        println("Title: ${problem.title}")
        println("Detail: ${problem.detail}")
    }
}
```

## Creating Your Own Implementation

You can create custom implementations for other HTTP clients:

```kotlin
class MyCustomHolidaysClient(
    private val httpClient: MyHttpClient
) : HolidaysClient {
    
    override suspend fun getCountries(languageIsoCode: String?): List<Country> {
        // Implement using your preferred HTTP client
        val response = httpClient.get("$baseUrl/Countries") {
            languageIsoCode?.let { param("languageIsoCode", it) }
        }
        return parseResponse(response)
    }
    
    // Implement other methods...
}
```

## Examples

See the `src/main/kotlin/org/openholidays/example` directory for complete examples:
- `Main.kt` - Ktor implementation example
- `spring/SpringExample.kt` - Spring WebClient example with Spring Boot integration patterns

## Architecture

```
┌─────────────────────────────────────────┐
│         Your Application                │
│  (Spring Boot, Ktor, Plain Kotlin...)  │
└────────────┬────────────────────────────┘
             │ depends on
             ▼
┌─────────────────────────────────────────┐
│     HolidaysClient Interface            │
│     (Framework-agnostic)                │
│  + Plain Kotlin data classes            │
│  + java.time.LocalDate only             │
└────────────┬────────────────────────────┘
             │ implemented by
       ┌─────┴──────┐
       ▼            ▼
┌─────────────┐  ┌──────────────────┐
│   Ktor      │  │ Spring WebClient │
│Implementation│  │  Implementation  │
└─────────────┘  └──────────────────┘
```

## Requirements

- Kotlin 1.9+
- JVM 17+ (for the library itself; JVM 11+ if you provide your own Spring dependencies)
- Uses `java.time.LocalDate` from JDK (no external date/time dependencies)
- (Optional) Ktor 3.1+ or Spring WebFlux 6.2+

## License

TBD

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## API Documentation

For detailed API documentation, visit [OpenHolidays API Documentation](https://openholidaysapi.org).

