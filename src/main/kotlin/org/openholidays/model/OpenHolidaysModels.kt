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

@Serializable
data class LocalizedText(
    val language: String,
    val text: String
)

@Serializable
data class CountryReference(
    val isoCode: String
)

@Serializable
data class SubdivisionReference(
    val code: String,
    val shortName: String
)

@Serializable
data class GroupReference(
    val code: String,
    val shortName: String
)

// --- Country, language, region models ---

@Serializable
data class CountryResponse(
    val isoCode: String,
    val name: List<LocalizedText>,
    val officialLanguages: List<String>
)

@Serializable
data class LanguageResponse(
    val isoCode: String,
    val name: List<LocalizedText>
)

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

@Serializable
enum class RegionalScope {
    @SerialName("National")
    NATIONAL,

    @SerialName("Regional")
    REGIONAL,

    @SerialName("Local")
    LOCAL
}

@Serializable
enum class TemporalScope {
    @SerialName("FullDay")
    FULL_DAY,

    @SerialName("HalfDay")
    HALF_DAY
}

// --- Holiday models ---

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

@Serializable
data class StatisticsResponse(
    @Serializable(with = LocalDateSerializer::class)
    val youngestStartDate: LocalDate,

    @Serializable(with = LocalDateSerializer::class)
    val oldestStartDate: LocalDate
)

// --- Problem details for error responses ---

@Serializable
data class ProblemDetails(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val instance: String? = null
)
