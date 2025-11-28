package org.openholidays.spring

import org.openholidays.HolidaysApiException
import org.openholidays.HolidaysClient
import org.openholidays.model.*
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import java.time.LocalDate

/**
 * Spring WebClient-based implementation of the HolidaysClient interface.
 *
 * This implementation uses Spring's WebClient for communication with the OpenHolidays API.
 * It's designed to work seamlessly with Spring Boot applications.
 *
 * All API methods are suspend functions and can be called from Kotlin coroutines.
 *
 * Example usage with Spring Boot:
 * ```kotlin
 * @Configuration
 * class HolidaysConfig {
 *     @Bean
 *     fun holidaysClient(): HolidaysClient {
 *         return SpringWebClientHolidaysClient.create()
 *     }
 * }
 *
 * @Service
 * class HolidayService(private val holidaysClient: HolidaysClient) {
 *     suspend fun getHolidays(country: String): List<Holiday> {
 *         return holidaysClient.getPublicHolidays(
 *             countryIsoCode = country,
 *             validFrom = LocalDate(2025, 1, 1),
 *             validTo = LocalDate(2025, 12, 31)
 *         )
 *     }
 * }
 * ```
 *
 * @property webClient The underlying Spring WebClient used for API requests
 */
class SpringWebClientHolidaysClient private constructor(
    private val webClient: WebClient
) : HolidaysClient {

    companion object {
        /** Default base URL for the OpenHolidays API */
        const val DEFAULT_BASE_URL: String = "https://openholidaysapi.org"

        /**
         * Creates a new SpringWebClientHolidaysClient with sensible defaults.
         *
         * The default client is configured with:
         * - Base URL pointing to the OpenHolidays API
         * - JSON content type handling
         * - Appropriate timeout settings
         *
         * @param baseUrl The base URL for the API (defaults to https://openholidaysapi.org)
         * @param webClientBuilder Optional pre-configured WebClient.Builder to customize the client
         * @return A new SpringWebClientHolidaysClient instance
         */
        fun create(
            baseUrl: String = DEFAULT_BASE_URL,
            webClientBuilder: WebClient.Builder = WebClient.builder()
        ): SpringWebClientHolidaysClient {
            val webClient = webClientBuilder
                .baseUrl(baseUrl.trimEnd('/'))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()

            return SpringWebClientHolidaysClient(webClient)
        }

        /**
         * Creates a new SpringWebClientHolidaysClient from an existing WebClient.
         *
         * This is useful when you want to share a WebClient instance with custom configuration
         * (e.g., with custom filters, codecs, or connection settings).
         *
         * @param webClient A pre-configured WebClient instance
         * @return A new SpringWebClientHolidaysClient instance
         */
        fun fromWebClient(webClient: WebClient): SpringWebClientHolidaysClient {
            return SpringWebClientHolidaysClient(webClient)
        }
    }

    override suspend fun getCountries(languageIsoCode: String?): List<Country> =
        get("/Countries", mapOf("languageIsoCode" to languageIsoCode))

    override suspend fun getLanguages(languageIsoCode: String?): List<Language> =
        get("/Languages", mapOf("languageIsoCode" to languageIsoCode))

    override suspend fun getSubdivisions(
        countryIsoCode: String,
        languageIsoCode: String?
    ): List<Subdivision> =
        get(
            "/Subdivisions",
            mapOf(
                "countryIsoCode" to countryIsoCode,
                "languageIsoCode" to languageIsoCode
            )
        )

    override suspend fun getGroups(
        countryIsoCode: String,
        languageIsoCode: String?,
        subdivisionCode: String?
    ): List<Group> =
        get(
            "/Groups",
            mapOf(
                "countryIsoCode" to countryIsoCode,
                "languageIsoCode" to languageIsoCode,
                "subdivisionCode" to subdivisionCode
            )
        )

    override suspend fun getPublicHolidays(
        countryIsoCode: String,
        validFrom: LocalDate,
        validTo: LocalDate,
        languageIsoCode: String?,
        subdivisionCode: String?
    ): List<Holiday> =
        get(
            "/PublicHolidays",
            mapOf(
                "countryIsoCode" to countryIsoCode,
                "validFrom" to validFrom.toString(),
                "validTo" to validTo.toString(),
                "languageIsoCode" to languageIsoCode,
                "subdivisionCode" to subdivisionCode
            )
        )

    override suspend fun getPublicHolidaysByDate(
        date: LocalDate,
        languageIsoCode: String?
    ): List<HolidayByDate> =
        get(
            "/PublicHolidaysByDate",
            mapOf(
                "date" to date.toString(),
                "languageIsoCode" to languageIsoCode
            )
        )

    override suspend fun getSchoolHolidays(
        countryIsoCode: String,
        validFrom: LocalDate,
        validTo: LocalDate,
        languageIsoCode: String?,
        subdivisionCode: String?,
        groupCode: String?
    ): List<Holiday> =
        get(
            "/SchoolHolidays",
            mapOf(
                "countryIsoCode" to countryIsoCode,
                "validFrom" to validFrom.toString(),
                "validTo" to validTo.toString(),
                "languageIsoCode" to languageIsoCode,
                "subdivisionCode" to subdivisionCode,
                "groupCode" to groupCode
            )
        )

    override suspend fun getSchoolHolidaysByDate(
        date: LocalDate,
        languageIsoCode: String?
    ): List<HolidayByDate> =
        get(
            "/SchoolHolidaysByDate",
            mapOf(
                "date" to date.toString(),
                "languageIsoCode" to languageIsoCode
            )
        )

    override suspend fun getPublicHolidayStatistics(
        countryIsoCode: String,
        subdivisionCode: String?
    ): List<Statistics> =
        get(
            "/Statistics/PublicHolidays",
            mapOf(
                "countryIsoCode" to countryIsoCode,
                "subdivisionCode" to subdivisionCode
            )
        )

    override suspend fun getSchoolHolidayStatistics(
        countryIsoCode: String,
        subdivisionCode: String?,
        groupCode: String?
    ): List<Statistics> =
        get(
            "/Statistics/SchoolHolidays",
            mapOf(
                "countryIsoCode" to countryIsoCode,
                "subdivisionCode" to subdivisionCode,
                "groupCode" to groupCode
            )
        )

    /**
     * Internal helper function to perform GET requests to the API.
     */
    private suspend inline fun <reified T> get(
        path: String,
        queryParams: Map<String, String?>
    ): T {
        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(path)
                queryParams.forEach { (key, value) ->
                    if (value != null) {
                        uriBuilder.queryParam(key, value)
                    }
                }
                uriBuilder.build()
            }
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                kotlinx.coroutines.reactor.mono {
                    val problem = response.awaitBodyOrNull<ProblemDetails>()
                    val message = buildString {
                        append("OpenHolidays API request failed with HTTP ")
                        append(response.statusCode().value())
                        problem?.let {
                            append(": ")
                            append(it.title ?: it.detail ?: "Unknown error")
                        }
                    }
                    throw HolidaysApiException(
                        statusCode = response.statusCode().value(),
                        problem = problem,
                        rawBody = null,
                        message = message
                    )
                }
            }
            .awaitBody()
    }
}

