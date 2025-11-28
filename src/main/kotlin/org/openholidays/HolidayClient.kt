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
 * Docs: https://openholidaysapi.org
 */
class OpenHolidaysClient private constructor(
    private val client: HttpClient,
    private val baseUrl: String
) : Closeable {

    companion object {
        const val DEFAULT_BASE_URL: String = "https://openholidaysapi.org"

        /**
         * Create a client with a sensible default HttpClient (CIO, JSON, logging).
         *
         * You can further tweak the underlying client via [configure].
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

    override fun close() {
        client.close()
    }

    // -------------------------------------------------------------------------
    // Public API surface (suspend functions)
    // -------------------------------------------------------------------------

    suspend fun getCountries(languageIsoCode: String? = null): List<CountryResponse> =
        get("/Countries", "languageIsoCode" to languageIsoCode)

    suspend fun getLanguages(languageIsoCode: String? = null): List<LanguageResponse> =
        get("/Languages", "languageIsoCode" to languageIsoCode)

    suspend fun getSubdivisions(
        countryIsoCode: String,
        languageIsoCode: String? = null
    ): List<SubdivisionResponse> =
        get(
            "/Subdivisions",
            "countryIsoCode" to countryIsoCode,
            "languageIsoCode" to languageIsoCode
        )

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

    suspend fun getPublicHolidaysByDate(
        date: LocalDate,
        languageIsoCode: String? = null
    ): List<HolidayByDateResponse> =
        get(
            "/PublicHolidaysByDate",
            "date" to date.toString(),
            "languageIsoCode" to languageIsoCode
        )

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

    suspend fun getSchoolHolidaysByDate(
        date: LocalDate,
        languageIsoCode: String? = null
    ): List<HolidayByDateResponse> =
        get(
            "/SchoolHolidaysByDate",
            "date" to date.toString(),
            "languageIsoCode" to languageIsoCode
        )

    suspend fun getPublicHolidayStatistics(
        countryIsoCode: String,
        subdivisionCode: String? = null
    ): List<StatisticsResponse> =
        get(
            "/Statistics/PublicHolidays",
            "countryIsoCode" to countryIsoCode,
            "subdivisionCode" to subdivisionCode
        )

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
