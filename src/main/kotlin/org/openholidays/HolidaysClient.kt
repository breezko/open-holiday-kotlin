package org.openholidays

import org.openholidays.model.*
import java.time.LocalDate

/**
 * Framework-agnostic interface for accessing the OpenHolidays API.
 *
 * This interface defines the contract for holiday data retrieval without
 * coupling to any specific HTTP client or serialization framework.
 *
 * Implementations should handle:
 * - HTTP communication
 * - JSON serialization/deserialization
 * - Error handling and mapping to HolidaysApiException
 *
 * All methods are suspend functions for Kotlin coroutines support.
 *
 * Example usage:
 * ```kotlin
 * val client: HolidaysClient = ... // obtain implementation
 * val holidays = client.getPublicHolidays(
 *     countryIsoCode = "US",
 *     validFrom = LocalDate(2025, 1, 1),
 *     validTo = LocalDate(2025, 12, 31)
 * )
 * ```
 *
 * API Documentation: https://openholidaysapi.org
 */
interface HolidaysClient {

    /**
     * Retrieves a list of all supported countries.
     *
     * @param languageIsoCode Optional ISO 639-1 language code (e.g., "EN", "DE") to localize country names
     * @return List of countries with their ISO codes, names, and official languages
     * @throws HolidaysApiException if the API request fails
     */
    suspend fun getCountries(languageIsoCode: String? = null): List<Country>

    /**
     * Retrieves a list of all supported languages.
     *
     * @param languageIsoCode Optional ISO 639-1 language code (e.g., "EN", "DE") to localize language names
     * @return List of languages with their ISO codes and localized names
     * @throws HolidaysApiException if the API request fails
     */
    suspend fun getLanguages(languageIsoCode: String? = null): List<Language>

    /**
     * Retrieves subdivisions (states, provinces, regions) for a specific country.
     *
     * @param countryIsoCode ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "FR")
     * @param languageIsoCode Optional ISO 639-1 language code to localize subdivision names
     * @return List of subdivisions with their codes, names, and other metadata
     * @throws HolidaysApiException if the API request fails
     */
    suspend fun getSubdivisions(
        countryIsoCode: String,
        languageIsoCode: String? = null
    ): List<Subdivision>

    /**
     * Retrieves groups (school types, regions) for a specific country.
     *
     * Groups are used to categorize school holidays by different educational levels or administrative regions.
     *
     * @param countryIsoCode ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "FR")
     * @param languageIsoCode Optional ISO 639-1 language code to localize group names
     * @param subdivisionCode Optional subdivision code to filter groups by subdivision
     * @return List of groups with their codes, names, and associated subdivisions
     * @throws HolidaysApiException if the API request fails
     */
    suspend fun getGroups(
        countryIsoCode: String,
        languageIsoCode: String? = null,
        subdivisionCode: String? = null
    ): List<Group>

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
     * @throws HolidaysApiException if the API request fails
     */
    suspend fun getPublicHolidays(
        countryIsoCode: String,
        validFrom: LocalDate,
        validTo: LocalDate,
        languageIsoCode: String? = null,
        subdivisionCode: String? = null
    ): List<Holiday>

    /**
     * Retrieves all public holidays occurring on a specific date across all countries.
     *
     * This endpoint is useful for finding out which countries observe a holiday on a particular day.
     *
     * @param date The date to query for public holidays
     * @param languageIsoCode Optional ISO 639-1 language code to localize holiday names
     * @return List of public holidays with country information for the specified date
     * @throws HolidaysApiException if the API request fails
     */
    suspend fun getPublicHolidaysByDate(
        date: LocalDate,
        languageIsoCode: String? = null
    ): List<HolidayByDate>

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
     * @throws HolidaysApiException if the API request fails
     */
    suspend fun getSchoolHolidays(
        countryIsoCode: String,
        validFrom: LocalDate,
        validTo: LocalDate,
        languageIsoCode: String? = null,
        subdivisionCode: String? = null,
        groupCode: String? = null
    ): List<Holiday>

    /**
     * Retrieves all school holidays occurring on a specific date across all countries.
     *
     * This endpoint is useful for finding out which countries have school holidays on a particular day.
     *
     * @param date The date to query for school holidays
     * @param languageIsoCode Optional ISO 639-1 language code to localize holiday names
     * @return List of school holidays with country information for the specified date
     * @throws HolidaysApiException if the API request fails
     */
    suspend fun getSchoolHolidaysByDate(
        date: LocalDate,
        languageIsoCode: String? = null
    ): List<HolidayByDate>

    /**
     * Retrieves statistics about public holidays for a specific country.
     *
     * Statistics include information about the oldest and youngest start dates of holidays in the dataset.
     *
     * @param countryIsoCode ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "FR")
     * @param subdivisionCode Optional subdivision code to filter statistics by region
     * @return List of statistics responses containing date range information
     * @throws HolidaysApiException if the API request fails
     */
    suspend fun getPublicHolidayStatistics(
        countryIsoCode: String,
        subdivisionCode: String? = null
    ): List<Statistics>

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
     * @throws HolidaysApiException if the API request fails
     */
    suspend fun getSchoolHolidayStatistics(
        countryIsoCode: String,
        subdivisionCode: String? = null,
        groupCode: String? = null
    ): List<Statistics>
}

/**
 * Exception thrown when a Holidays API request fails.
 *
 * This exception is framework-agnostic and can be created by any implementation.
 *
 * @property statusCode The HTTP status code returned by the API
 * @property problem Parsed problem details following RFC 7807, if available in the response
 * @property rawBody The raw response body as a string
 */
class HolidaysApiException(
    val statusCode: Int,
    val problem: ProblemDetails?,
    val rawBody: String?,
    message: String
) : Exception(message)

