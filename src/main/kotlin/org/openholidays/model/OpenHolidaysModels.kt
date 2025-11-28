package org.openholidays.model


import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// --- Date serializer (ISO-8601, e.g. 2023-01-01) ---

/**
 * Custom serializer for kotlinx.datetime.LocalDate.
 *
 * Serializes and deserializes dates in ISO-8601 format (yyyy-MM-dd).
 * This ensures dates are properly handled in JSON responses from the OpenHolidays API.
 */
object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate =
        LocalDate.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: LocalDate) {
        // LocalDate.toString() -> "yyyy-MM-dd"
        encoder.encodeString(value.toString())
    }
}

// --- Core value objects ---

/**
 * Represents a text string with its associated language code.
 *
 * Used to provide multilingual support for names, comments, and descriptions.
 *
 * @property language ISO 639-1 language code (e.g., "EN", "DE", "FR")
 * @property text The localized text content
 */
@Serializable
data class LocalizedText(
    val language: String,
    val text: String
)

/**
 * Reference to a country by its ISO code.
 *
 * @property isoCode ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "FR")
 */
@Serializable
data class CountryReference(
    val isoCode: String
)

/**
 * Reference to a subdivision (state, province, region) within a country.
 *
 * @property code The subdivision code
 * @property shortName Short name or abbreviation of the subdivision
 */
@Serializable
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
@Serializable
data class GroupReference(
    val code: String,
    val shortName: String
)

// --- Country, language, region models ---

/**
 * Represents a country with its basic information.
 *
 * @property isoCode ISO 3166-1 alpha-2 country code
 * @property name Localized names of the country in different languages
 * @property officialLanguages List of official language codes for this country
 */
@Serializable
data class CountryResponse(
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
@Serializable
data class LanguageResponse(
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
@Serializable
data class GroupResponse(
    val category: List<LocalizedText>,
    val children: List<GroupResponse>? = null,
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
@Serializable
data class SubdivisionResponse(
    val category: List<LocalizedText>,
    val children: List<SubdivisionResponse>? = null,
    val code: String,
    val comment: List<LocalizedText>? = null,
    val groups: List<GroupReference>? = null,
    val isoCode: String? = null,
    val name: List<LocalizedText>,
    val officialLanguages: List<String>,
    val shortName: String
)

// --- Enums ---

/**
 * Tags that provide additional metadata about holidays.
 *
 * - RECOMMENDED: Recommended holiday observance
 * - PROVISIONAL: Provisional or tentative holiday
 * - ONE_TIME: One-time or special holiday
 * - EXCEPTION: Exception to normal holiday rules
 */
@Serializable
enum class HolidayTags {
    @SerialName("Recommended")
    RECOMMENDED,

    @SerialName("Provisional")
    PROVISIONAL,

    @SerialName("OneTime")
    ONE_TIME,

    @SerialName("Exception")
    EXCEPTION
}

/**
 * Types of holidays recognized by the OpenHolidays API.
 *
 * - PUBLIC: Public or national holiday
 * - BANK: Bank holiday
 * - OPTIONAL: Optional or observance holiday
 * - SCHOOL: School holiday/break
 * - BACK_TO_SCHOOL: Start of school term
 * - END_OF_LESSONS: End of school lessons
 */
@Serializable
enum class HolidayType {
    @SerialName("Public")
    PUBLIC,

    @SerialName("Bank")
    BANK,

    @SerialName("Optional")
    OPTIONAL,

    @SerialName("School")
    SCHOOL,

    @SerialName("BackToSchool")
    BACK_TO_SCHOOL,

    @SerialName("EndOfLessons")
    END_OF_LESSONS
}

/**
 * Geographic scope of a holiday.
 *
 * - NATIONAL: Observed throughout the entire country
 * - REGIONAL: Observed in specific regions or subdivisions
 * - LOCAL: Observed in specific local areas
 */
@Serializable
enum class RegionalScope {
    @SerialName("National")
    NATIONAL,

    @SerialName("Regional")
    REGIONAL,

    @SerialName("Local")
    LOCAL
}

/**
 * Time scope or duration of a holiday.
 *
 * - FULL_DAY: Entire day holiday
 * - HALF_DAY: Half-day holiday or partial observance
 */
@Serializable
enum class TemporalScope {
    @SerialName("FullDay")
    FULL_DAY,

    @SerialName("HalfDay")
    HALF_DAY
}

// --- Holiday models ---

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
@Serializable
data class HolidayResponse(
    val comment: List<LocalizedText>? = null,

    @Serializable(with = LocalDateSerializer::class)
    val endDate: LocalDate,

    val id: String,
    val name: List<LocalizedText>,
    val nationwide: Boolean,
    val regionalScope: RegionalScope? = null,

    @Serializable(with = LocalDateSerializer::class)
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
@Serializable
data class HolidayByDateResponse(
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

// --- Statistics ---

/**
 * Represents statistics about holidays in the dataset.
 *
 * Provides information about the date range of available holiday data.
 *
 * @property youngestStartDate The most recent start date in the dataset
 * @property oldestStartDate The oldest start date in the dataset
 */
@Serializable
data class StatisticsResponse(
    @Serializable(with = LocalDateSerializer::class)
    val youngestStartDate: LocalDate,

    @Serializable(with = LocalDateSerializer::class)
    val oldestStartDate: LocalDate
)

// --- Problem details for error responses ---

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
@Serializable
data class ProblemDetails(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val instance: String? = null
)
