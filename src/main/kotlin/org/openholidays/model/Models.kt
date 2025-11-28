package org.openholidays.model

import kotlinx.datetime.LocalDate

/**
 * Represents a text string with its associated language code.
 *
 * Used to provide multilingual support for names, comments, and descriptions.
 *
 * @property language ISO 639-1 language code (e.g., "EN", "DE", "FR")
 * @property text The localized text content
 */
data class LocalizedText(
    val language: String,
    val text: String
)

/**
 * Reference to a country by its ISO code.
 *
 * @property isoCode ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "FR")
 */
data class CountryReference(
    val isoCode: String
)

/**
 * Reference to a subdivision (state, province, region) within a country.
 *
 * @property code The subdivision code
 * @property shortName Short name or abbreviation of the subdivision
 */
data class SubdivisionReference(
    val code: String,
    val shortName: String
)

/**
 * Reference to a group (school type, educational level) within a country.
 *
 * @property code The group code
 * @property shortName Short name or abbreviation of the group
 */
data class GroupReference(
    val code: String,
    val shortName: String
)

/**
 * Represents a country with its basic information.
 *
 * @property isoCode ISO 3166-1 alpha-2 country code
 * @property name Localized names of the country in different languages
 * @property officialLanguages List of official language codes for this country
 */
data class Country(
    val isoCode: String,
    val name: List<LocalizedText>,
    val officialLanguages: List<String>
)

/**
 * Represents a language with its localized names.
 *
 * @property isoCode ISO 639-1 language code
 * @property name Localized names of the language in different languages
 */
data class Language(
    val isoCode: String,
    val name: List<LocalizedText>
)

/**
 * Represents a group (school type, educational level) within a country.
 *
 * Groups can have hierarchical structure with parent-child relationships.
 *
 * @property category Localized category descriptions
 * @property children Optional list of child groups (for hierarchical structures)
 * @property code Unique code identifying this group
 * @property comment Optional localized comments or notes
 * @property name Localized names of the group
 * @property shortName Short name or abbreviation
 * @property subdivisions Optional list of subdivisions this group applies to
 */
data class Group(
    val category: List<LocalizedText>,
    val children: List<Group>? = null,
    val code: String,
    val comment: List<LocalizedText>? = null,
    val name: List<LocalizedText>,
    val shortName: String,
    val subdivisions: List<SubdivisionReference>? = null
)

/**
 * Represents a subdivision (state, province, region) within a country.
 *
 * Subdivisions can have hierarchical structure with parent-child relationships.
 *
 * @property category Localized category descriptions
 * @property children Optional list of child subdivisions (for hierarchical structures)
 * @property code Unique code identifying this subdivision
 * @property comment Optional localized comments or notes
 * @property groups Optional list of groups (school types) this subdivision is associated with
 * @property isoCode Optional ISO 3166-2 subdivision code
 * @property name Localized names of the subdivision
 * @property officialLanguages List of official language codes for this subdivision
 * @property shortName Short name or abbreviation
 */
data class Subdivision(
    val category: List<LocalizedText>,
    val children: List<Subdivision>? = null,
    val code: String,
    val comment: List<LocalizedText>? = null,
    val groups: List<GroupReference>? = null,
    val isoCode: String? = null,
    val name: List<LocalizedText>,
    val officialLanguages: List<String>,
    val shortName: String
)

/**
 * Tags that provide additional metadata about holidays.
 */
enum class HolidayTags {
    RECOMMENDED,
    PROVISIONAL,
    ONE_TIME,
    EXCEPTION
}

/**
 * Types of holidays recognized by the OpenHolidays API.
 */
enum class HolidayType {
    PUBLIC,
    BANK,
    OPTIONAL,
    SCHOOL,
    BACK_TO_SCHOOL,
    END_OF_LESSONS
}

/**
 * Geographic scope of a holiday.
 */
enum class RegionalScope {
    NATIONAL,
    REGIONAL,
    LOCAL
}

/**
 * Time scope or duration of a holiday.
 */
enum class TemporalScope {
    FULL_DAY,
    HALF_DAY
}

/**
 * Represents a holiday with all its details.
 *
 * This is the primary response type for holiday queries, containing comprehensive
 * information about public and school holidays.
 *
 * @property comment Optional localized comments or notes about this holiday
 * @property endDate The last date of the holiday period (inclusive)
 * @property id Unique identifier for this holiday
 * @property name Localized names of the holiday
 * @property nationwide Whether this holiday is observed nationwide
 * @property regionalScope Geographic scope of the holiday (national, regional, or local)
 * @property startDate The first date of the holiday period (inclusive)
 * @property subdivisions Optional list of subdivisions where this holiday is observed
 * @property groups Optional list of groups (school types) this holiday applies to
 * @property temporalScope Duration scope (full-day or half-day)
 * @property type Type of holiday (public, bank, school, etc.)
 */
data class Holiday(
    val comment: List<LocalizedText>? = null,
    val endDate: LocalDate,
    val id: String,
    val name: List<LocalizedText>,
    val nationwide: Boolean,
    val regionalScope: RegionalScope? = null,
    val startDate: LocalDate,
    val subdivisions: List<SubdivisionReference>? = null,
    val groups: List<GroupReference>? = null,
    val temporalScope: TemporalScope? = null,
    val type: HolidayType
)

/**
 * Represents a holiday occurring on a specific date, including country information.
 *
 * This response type is used when querying holidays by date across multiple countries.
 * It includes a reference to the country where the holiday is observed.
 *
 * @property comment Optional localized comments or notes about this holiday
 * @property country Reference to the country where this holiday is observed
 * @property groups Optional list of groups (school types) this holiday applies to
 * @property id Unique identifier for this holiday
 * @property name Localized names of the holiday
 * @property nationwide Whether this holiday is observed nationwide in its country
 * @property regionalScope Geographic scope of the holiday (national, regional, or local)
 * @property subdivisions Optional list of subdivisions where this holiday is observed
 * @property tags Optional metadata tags (recommended, provisional, one-time, exception)
 * @property temporalScope Duration scope (full-day or half-day)
 * @property type Type of holiday (public, bank, school, etc.)
 */
data class HolidayByDate(
    val comment: List<LocalizedText>? = null,
    val country: CountryReference,
    val groups: List<GroupReference>? = null,
    val id: String,
    val name: List<LocalizedText>,
    val nationwide: Boolean,
    val regionalScope: RegionalScope? = null,
    val subdivisions: List<SubdivisionReference>? = null,
    val tags: HolidayTags? = null,
    val temporalScope: TemporalScope? = null,
    val type: HolidayType
)

/**
 * Represents statistics about holidays in the dataset.
 *
 * Provides information about the date range of available holiday data.
 *
 * @property youngestStartDate The most recent start date in the dataset
 * @property oldestStartDate The oldest start date in the dataset
 */
data class Statistics(
    val youngestStartDate: LocalDate,
    val oldestStartDate: LocalDate
)

/**
 * RFC 7807 Problem Details for HTTP APIs.
 *
 * Used to convey machine-readable error information in API responses.
 *
 * @property type URI reference identifying the problem type
 * @property title Short, human-readable summary of the problem
 * @property status HTTP status code
 * @property detail Human-readable explanation specific to this occurrence
 * @property instance URI reference identifying the specific occurrence of the problem
 */
data class ProblemDetails(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val instance: String? = null
)

