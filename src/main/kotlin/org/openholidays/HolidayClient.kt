package org.openholidays

import org.openholidays.model.CountryResponse
import org.openholidays.model.GroupResponse
import org.openholidays.model.HolidayByDateResponse
import org.openholidays.model.HolidayResponse
import org.openholidays.model.LanguageResponse
import org.openholidays.model.ProblemDetails
import org.openholidays.model.StatisticsResponse
import org.openholidays.model.SubdivisionResponse

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import java.io.Closeable
import java.io.IOException

/**
 * Idiomatic Kotlin client for the OpenHolidays API.
 *
 * This client provides access to public and school holiday information from around the world.
 * All API methods are suspend functions and can be called from coroutines.
 *
 * Example usage:
 * ```kotlin
 * val client = OpenHolidaysClient.create()
 * try {
 *     val holidays = client.getPublicHolidays(
 *         countryIsoCode = "US",
 *         validFrom = LocalDate(2025, 1, 1),
 *         validTo = LocalDate(2025, 12, 31)
 *     )
 *     holidays.forEach { println(it.name) }
 * } finally {
 *     client.close()
 * }
 * ```
 *
 * API Documentation: https://openholidaysapi.org
 *
 * @property client The underlying Ktor HttpClient used for API requests
 * @property baseUrl The base URL of the OpenHolidays API
 */
class OpenHolidaysClient private constructor(
    private val client: HttpClient,
    private val baseUrl: String
) : Closeable {

    companion object {
        /** Default base URL for the OpenHolidays API */
        const val DEFAULT_BASE_URL: String = "https://openholidaysapi.org"

        /**
         * Creates a new OpenHolidaysClient with sensible defaults.
         *
         * The default client includes:
         * - CIO engine for HTTP requests
         * - JSON content negotiation with kotlinx.serialization
         * - Request/response logging at INFO level
         * - Automatic handling of unknown JSON properties
         *
         * @param baseUrl The base URL for the API (defaults to https://openholidaysapi.org)
         * @param configure Optional lambda to further configure the underlying HttpClient
         * @return A new OpenHolidaysClient instance
         */
        fun create(
            baseUrl: String = DEFAULT_BASE_URL,
            configure: HttpClientConfig<*>.() -> Unit = {}
        ): OpenHolidaysClient {
            val jsonConfig = Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }

            val httpClient = HttpClient(CIO) {
                expectSuccess = false

                install(ContentNegotiation) {
                    json(
                        contentType = ContentType.Application.Json,
                        json = jsonConfig
                    )
                    json(
                        contentType = ContentType.parse("text/json"),
                        json = jsonConfig
                    )
                }

                install(Logging) {
                    level = LogLevel.INFO
                }

                configure()
            }

            return OpenHolidaysClient(
                client = httpClient,
                baseUrl = baseUrl.trimEnd('/')
            )
        }
    }

    /**
     * Closes the underlying HTTP client and releases resources.
     *
     * After calling this method, the client should not be used for further API calls.
     * This method should be called when you're done using the client, ideally in a try-finally block.
     */
    override fun close() {
        client.close()
    }

    // -------------------------------------------------------------------------
    // Public API surface (suspend functions)
    // -------------------------------------------------------------------------

    /**
     * Retrieves a list of all supported countries.
     *
     * @param languageIsoCode Optional ISO 639-1 language code (e.g., "EN", "DE") to localize country names
     * @return List of countries with their ISO codes, names, and official languages
     * @throws OpenHolidaysApiException if the API request fails
     */
    suspend fun getCountries(languageIsoCode: String? = null): List<CountryResponse> =
        get("/Countries", "languageIsoCode" to languageIsoCode)

    /**
     * Retrieves a list of all supported languages.
     *
     * @param languageIsoCode Optional ISO 639-1 language code (e.g., "EN", "DE") to localize language names
     * @return List of languages with their ISO codes and localized names
     * @throws OpenHolidaysApiException if the API request fails
     */
    suspend fun getLanguages(languageIsoCode: String? = null): List<LanguageResponse> =
        get("/Languages", "languageIsoCode" to languageIsoCode)

    /**
     * Retrieves subdivisions (states, provinces, regions) for a specific country.
     *
     * @param countryIsoCode ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "FR")
     * @param languageIsoCode Optional ISO 639-1 language code to localize subdivision names
     * @return List of subdivisions with their codes, names, and other metadata
     * @throws OpenHolidaysApiException if the API request fails
     */
    suspend fun getSubdivisions(
        countryIsoCode: String,
        languageIsoCode: String? = null
    ): List<SubdivisionResponse> =
        get(
            "/Subdivisions",
            "countryIsoCode" to countryIsoCode,
            "languageIsoCode" to languageIsoCode
        )

    /**
     * Retrieves groups (school types, regions) for a specific country.
     *
     * Groups are used to categorize school holidays by different educational levels or administrative regions.
     *
     * @param countryIsoCode ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "FR")
     * @param languageIsoCode Optional ISO 639-1 language code to localize group names
     * @param subdivisionCode Optional subdivision code to filter groups by subdivision
     * @return List of groups with their codes, names, and associated subdivisions
     * @throws OpenHolidaysApiException if the API request fails
     */
    suspend fun getGroups(
        countryIsoCode: String,
        languageIsoCode: String? = null,
        subdivisionCode: String? = null
    ): List<GroupResponse> =
        get(
            "/Groups",
            "countryIsoCode" to countryIsoCode,
            "languageIsoCode" to languageIsoCode,
            "subdivisionCode" to subdivisionCode
        )

    /**
     * Retrieves public holidays for a specific country within a date range.
     *
     * Public holidays include national holidays, bank holidays, and regional observances.
     *
     * @param countryIsoCode ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "FR")
     * @param validFrom Start date of the query range (inclusive)
     * @param validTo End date of the query range (inclusive)
     * @param languageIsoCode Optional ISO 639-1 language code to localize holiday names
     * @param subdivisionCode Optional subdivision code to filter holidays by region
     * @return List of public holidays with names, dates, and metadata
     * @throws OpenHolidaysApiException if the API request fails
     */
    suspend fun getPublicHolidays(
        countryIsoCode: String,
        validFrom: LocalDate,
        validTo: LocalDate,
        languageIsoCode: String? = null,
        subdivisionCode: String? = null
    ): List<HolidayResponse> =
        get(
            "/PublicHolidays",
            "countryIsoCode" to countryIsoCode,
            "validFrom" to validFrom.toString(),
            "validTo" to validTo.toString(),
            "languageIsoCode" to languageIsoCode,
            "subdivisionCode" to subdivisionCode
        )

    /**
     * Retrieves all public holidays occurring on a specific date across all countries.
     *
     * This endpoint is useful for finding out which countries observe a holiday on a particular day.
     *
     * @param date The date to query for public holidays
     * @param languageIsoCode Optional ISO 639-1 language code to localize holiday names
     * @return List of public holidays with country information for the specified date
     * @throws OpenHolidaysApiException if the API request fails
     */
    suspend fun getPublicHolidaysByDate(
        date: LocalDate,
        languageIsoCode: String? = null
    ): List<HolidayByDateResponse> =
        get(
            "/PublicHolidaysByDate",
            "date" to date.toString(),
            "languageIsoCode" to languageIsoCode
        )

    /**
     * Retrieves school holidays for a specific country within a date range.
     *
     * School holidays can be filtered by subdivision (e.g., state/province) and group (e.g., school type).
     *
     * @param countryIsoCode ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "FR")
     * @param validFrom Start date of the query range (inclusive)
     * @param validTo End date of the query range (inclusive)
     * @param languageIsoCode Optional ISO 639-1 language code to localize holiday names
     * @param subdivisionCode Optional subdivision code to filter holidays by region
     * @param groupCode Optional group code to filter holidays by school type or educational level
     * @return List of school holidays with names, dates, and metadata
     * @throws OpenHolidaysApiException if the API request fails
     */
    suspend fun getSchoolHolidays(
        countryIsoCode: String,
        validFrom: LocalDate,
        validTo: LocalDate,
        languageIsoCode: String? = null,
        subdivisionCode: String? = null,
        groupCode: String? = null
    ): List<HolidayResponse> =
        get(
            "/SchoolHolidays",
            "countryIsoCode" to countryIsoCode,
            "validFrom" to validFrom.toString(),
            "validTo" to validTo.toString(),
            "languageIsoCode" to languageIsoCode,
            "subdivisionCode" to subdivisionCode,
            "groupCode" to groupCode
        )

    /**
     * Retrieves all school holidays occurring on a specific date across all countries.
     *
     * This endpoint is useful for finding out which countries have school holidays on a particular day.
     *
     * @param date The date to query for school holidays
     * @param languageIsoCode Optional ISO 639-1 language code to localize holiday names
     * @return List of school holidays with country information for the specified date
     * @throws OpenHolidaysApiException if the API request fails
     */
    suspend fun getSchoolHolidaysByDate(
        date: LocalDate,
        languageIsoCode: String? = null
    ): List<HolidayByDateResponse> =
        get(
            "/SchoolHolidaysByDate",
            "date" to date.toString(),
            "languageIsoCode" to languageIsoCode
        )

    /**
     * Retrieves statistics about public holidays for a specific country.
     *
     * Statistics include information about the oldest and youngest start dates of holidays in the dataset.
     *
     * @param countryIsoCode ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "FR")
     * @param subdivisionCode Optional subdivision code to filter statistics by region
     * @return List of statistics responses containing date range information
     * @throws OpenHolidaysApiException if the API request fails
     */
    suspend fun getPublicHolidayStatistics(
        countryIsoCode: String,
        subdivisionCode: String? = null
    ): List<StatisticsResponse> =
        get(
            "/Statistics/PublicHolidays",
            "countryIsoCode" to countryIsoCode,
            "subdivisionCode" to subdivisionCode
        )

    /**
     * Retrieves statistics about school holidays for a specific country.
     *
     * Statistics include information about the oldest and youngest start dates of holidays in the dataset.
     * Can be filtered by subdivision and/or group.
     *
     * @param countryIsoCode ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "FR")
     * @param subdivisionCode Optional subdivision code to filter statistics by region
     * @param groupCode Optional group code to filter statistics by school type or educational level
     * @return List of statistics responses containing date range information
     * @throws OpenHolidaysApiException if the API request fails
     */
    suspend fun getSchoolHolidayStatistics(
        countryIsoCode: String,
        subdivisionCode: String? = null,
        groupCode: String? = null
    ): List<StatisticsResponse> =
        get(
            "/Statistics/SchoolHolidays",
            "countryIsoCode" to countryIsoCode,
            "subdivisionCode" to subdivisionCode,
            "groupCode" to groupCode
        )

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Internal helper function to perform GET requests to the API.
     *
     * Constructs the URL with query parameters, makes the request, and handles the response.
     * Automatically deserializes successful responses and throws exceptions for failures.
     *
     * @param T The expected response type
     * @param path The API endpoint path (e.g., "/Countries")
     * @param queryParams Variable number of key-value pairs for query parameters (null values are filtered out)
     * @return The deserialized response object
     * @throws OpenHolidaysApiException if the API returns a non-success status code
     */
    private suspend inline fun <reified T> get(
        path: String,
        vararg queryParams: Pair<String, String?>
    ): T {
        val response: HttpResponse = client.get {
            url(baseUrl + path)
            queryParams.forEach { (key, value) ->
                if (value != null) {
                    parameter(key, value)
                }
            }
        }

        if (response.status.isSuccess()) {
            return response.body()
        } else {
            throw OpenHolidaysApiException.fromResponse(response)
        }
    }
}

// -----------------------------------------------------------------------------
// Exception type for failed calls
// -----------------------------------------------------------------------------

/**
 * Exception thrown when an OpenHolidays API request fails.
 *
 * This exception captures the HTTP status code, problem details (if available),
 * and the raw response body for debugging purposes.
 *
 * @property statusCode The HTTP status code returned by the API
 * @property problem Parsed problem details following RFC 7807, if available in the response
 * @property rawBody The raw response body as a string
 */
class OpenHolidaysApiException(
    val statusCode: Int,
    val problem: ProblemDetails?,
    val rawBody: String?
) : IOException(
    buildString {
        append("OpenHolidays API request failed with HTTP ")
        append(statusCode)
        problem?.let {
            append(": ")
            append(it.title ?: it.detail ?: "Unknown error")
        }
    }
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        /**
         * Creates an OpenHolidaysApiException from an HTTP response.
         *
         * Attempts to parse the response body as RFC 7807 problem details.
         * If parsing fails or the body is empty, the exception will still contain
         * the status code and raw body.
         *
         * @param response The HTTP response from a failed API call
         * @return A new OpenHolidaysApiException instance with parsed error information
         */
        suspend fun fromResponse(response: HttpResponse): OpenHolidaysApiException {
            val bodyText = response.bodyAsText()
            val problem = runCatching {
                if (bodyText.isNotBlank()) {
                    json.decodeFromString(ProblemDetails.serializer(), bodyText)
                } else {
                    null
                }
            }.getOrNull()

            return OpenHolidaysApiException(
                statusCode = response.status.value,
                problem = problem,
                rawBody = bodyText
            )
        }
    }
}
