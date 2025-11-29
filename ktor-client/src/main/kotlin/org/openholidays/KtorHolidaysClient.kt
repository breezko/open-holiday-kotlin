package org.openholidays.ktor

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.openholidays.HolidaysApiException
import org.openholidays.HolidaysClient
import org.openholidays.model.*
import java.io.Closeable
import java.time.LocalDate

/**
 * Ktor-based implementation of the HolidaysClient interface.
 *
 * This implementation uses Ktor HTTP client for communication with the OpenHolidays API.
 * All API methods are suspend functions and can be called from coroutines.
 *
 * Example usage:
 * ```kotlin
 * val client = KtorHolidaysClient.create()
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
 * @property client The underlying Ktor HttpClient used for API requests
 * @property baseUrl The base URL of the OpenHolidays API
 */
class KtorHolidaysClient private constructor(
    private val client: HttpClient,
    private val baseUrl: String
) : HolidaysClient, Closeable {

    companion object {
        /** Default base URL for the OpenHolidays API */
        const val DEFAULT_BASE_URL: String = "https://openholidaysapi.org"

        private val errorJsonParser = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        /**
         * Creates a new KtorHolidaysClient with sensible defaults.
         *
         * The default client includes:
         * - CIO engine for HTTP requests
         * - JSON content negotiation with kotlinx.serialization
         * - Request/response logging at INFO level
         * - Automatic handling of unknown JSON properties
         *
         * @param baseUrl The base URL for the API (defaults to https://openholidaysapi.org)
         * @param configure Optional lambda to further configure the underlying HttpClient
         * @return A new KtorHolidaysClient instance
         */
        fun create(
            baseUrl: String = DEFAULT_BASE_URL,
            configure: HttpClientConfig<*>.() -> Unit = {}
        ): KtorHolidaysClient {
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

            return KtorHolidaysClient(
                client = httpClient,
                baseUrl = baseUrl.trimEnd('/')
            )
        }
    }

    /**
     * Closes the underlying HTTP client and releases resources.
     *
     * After calling this method, the client should not be used for further API calls.
     */
    override fun close() {
        client.close()
    }

    override suspend fun getCountries(languageIsoCode: String?): List<Country> =
        get<List<CountryDto>>("/Countries", "languageIsoCode" to languageIsoCode)
            .map { it.toDomain() }

    override suspend fun getLanguages(languageIsoCode: String?): List<Language> =
        get<List<LanguageDto>>("/Languages", "languageIsoCode" to languageIsoCode)
            .map { it.toDomain() }

    override suspend fun getSubdivisions(
        countryIsoCode: String,
        languageIsoCode: String?
    ): List<Subdivision> =
        get<List<SubdivisionDto>>(
            "/Subdivisions",
            "countryIsoCode" to countryIsoCode,
            "languageIsoCode" to languageIsoCode
        ).map { it.toDomain() }

    override suspend fun getGroups(
        countryIsoCode: String,
        languageIsoCode: String?,
        subdivisionCode: String?
    ): List<Group> =
        get<List<GroupDto>>(
            "/Groups",
            "countryIsoCode" to countryIsoCode,
            "languageIsoCode" to languageIsoCode,
            "subdivisionCode" to subdivisionCode
        ).map { it.toDomain() }

    override suspend fun getPublicHolidays(
        countryIsoCode: String,
        validFrom: LocalDate,
        validTo: LocalDate,
        languageIsoCode: String?,
        subdivisionCode: String?
    ): List<Holiday> =
        get<List<HolidayDto>>(
            "/PublicHolidays",
            "countryIsoCode" to countryIsoCode,
            "validFrom" to validFrom.toString(),
            "validTo" to validTo.toString(),
            "languageIsoCode" to languageIsoCode,
            "subdivisionCode" to subdivisionCode
        ).map { it.toDomain() }

    override suspend fun getPublicHolidaysByDate(
        date: LocalDate,
        languageIsoCode: String?
    ): List<HolidayByDate> =
        get<List<HolidayByDateDto>>(
            "/PublicHolidaysByDate",
            "date" to date.toString(),
            "languageIsoCode" to languageIsoCode
        ).map { it.toDomain() }

    override suspend fun getSchoolHolidays(
        countryIsoCode: String,
        validFrom: LocalDate,
        validTo: LocalDate,
        languageIsoCode: String?,
        subdivisionCode: String?,
        groupCode: String?
    ): List<Holiday> =
        get<List<HolidayDto>>(
            "/SchoolHolidays",
            "countryIsoCode" to countryIsoCode,
            "validFrom" to validFrom.toString(),
            "validTo" to validTo.toString(),
            "languageIsoCode" to languageIsoCode,
            "subdivisionCode" to subdivisionCode,
            "groupCode" to groupCode
        ).map { it.toDomain() }

    override suspend fun getSchoolHolidaysByDate(
        date: LocalDate,
        languageIsoCode: String?
    ): List<HolidayByDate> =
        get<List<HolidayByDateDto>>(
            "/SchoolHolidaysByDate",
            "date" to date.toString(),
            "languageIsoCode" to languageIsoCode
        ).map { it.toDomain() }

    override suspend fun getPublicHolidayStatistics(
        countryIsoCode: String,
        subdivisionCode: String?
    ): List<Statistics> =
        get<List<StatisticsDto>>(
            "/Statistics/PublicHolidays",
            "countryIsoCode" to countryIsoCode,
            "subdivisionCode" to subdivisionCode
        ).map { it.toDomain() }

    override suspend fun getSchoolHolidayStatistics(
        countryIsoCode: String,
        subdivisionCode: String?,
        groupCode: String?
    ): List<Statistics> =
        get<List<StatisticsDto>>(
            "/Statistics/SchoolHolidays",
            "countryIsoCode" to countryIsoCode,
            "subdivisionCode" to subdivisionCode,
            "groupCode" to groupCode
        ).map { it.toDomain() }

    /**
     * Internal helper function to perform GET requests to the API.
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
            throw createException(response)
        }
    }

    private suspend fun createException(response: HttpResponse): HolidaysApiException {
        val bodyText = response.bodyAsText()
        val problem = runCatching {
            if (bodyText.isNotBlank()) {
                errorJsonParser.decodeFromString(ProblemDetailsDto.serializer(), bodyText).toDomain()
            } else {
                null
            }
        }.getOrNull()

        val message = buildString {
            append("OpenHolidays API request failed with HTTP ")
            append(response.status.value)
            problem?.let {
                append(": ")
                append(it.title ?: it.detail ?: "Unknown error")
            }
        }

        return HolidaysApiException(
            statusCode = response.status.value,
            problem = problem,
            rawBody = bodyText,
            message = message
        )
    }
}

