package org.openholidays.ktor

import kotlinx.datetime.LocalDate as KotlinxLocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.openholidays.model.*
import java.time.LocalDate

/**
 * Internal models for Ktor serialization.
 * These models map directly to the API JSON structure.
 */

// Conversion utilities between java.time and kotlinx.datetime
internal fun LocalDate.toKotlinx(): KotlinxLocalDate =
    KotlinxLocalDate(this.year, this.monthValue, this.dayOfMonth)

internal fun KotlinxLocalDate.toJava(): LocalDate =
    LocalDate.of(this.year, this.monthNumber, this.dayOfMonth)

internal object LocalDateSerializer : KSerializer<KotlinxLocalDate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): KotlinxLocalDate =
        KotlinxLocalDate.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: KotlinxLocalDate) {
        encoder.encodeString(value.toString())
    }
}

@Serializable
internal data class LocalizedTextDto(
    val language: String,
    val text: String
)

@Serializable
internal data class CountryReferenceDto(
    val isoCode: String
)

@Serializable
internal data class SubdivisionReferenceDto(
    val code: String,
    val shortName: String
)

@Serializable
internal data class GroupReferenceDto(
    val code: String,
    val shortName: String
)

@Serializable
internal data class CountryDto(
    val isoCode: String,
    val name: List<LocalizedTextDto>,
    val officialLanguages: List<String>
)

@Serializable
internal data class LanguageDto(
    val isoCode: String,
    val name: List<LocalizedTextDto>
)

@Serializable
internal data class GroupDto(
    val category: List<LocalizedTextDto>,
    val children: List<GroupDto>? = null,
    val code: String,
    val comment: List<LocalizedTextDto>? = null,
    val name: List<LocalizedTextDto>,
    val shortName: String,
    val subdivisions: List<SubdivisionReferenceDto>? = null
)

@Serializable
internal data class SubdivisionDto(
    val category: List<LocalizedTextDto>,
    val children: List<SubdivisionDto>? = null,
    val code: String,
    val comment: List<LocalizedTextDto>? = null,
    val groups: List<GroupReferenceDto>? = null,
    val isoCode: String? = null,
    val name: List<LocalizedTextDto>,
    val officialLanguages: List<String>,
    val shortName: String
)

@Serializable
internal enum class HolidayTagsDto {
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
internal enum class HolidayTypeDto {
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
internal enum class RegionalScopeDto {
    @SerialName("National")
    NATIONAL,

    @SerialName("Regional")
    REGIONAL,

    @SerialName("Local")
    LOCAL
}

@Serializable
internal enum class TemporalScopeDto {
    @SerialName("FullDay")
    FULL_DAY,

    @SerialName("HalfDay")
    HALF_DAY
}

@Serializable
internal data class HolidayDto(
    val comment: List<LocalizedTextDto>? = null,

    @Serializable(with = LocalDateSerializer::class)
    val endDate: KotlinxLocalDate,

    val id: String,
    val name: List<LocalizedTextDto>,
    val nationwide: Boolean,
    val regionalScope: RegionalScopeDto? = null,

    @Serializable(with = LocalDateSerializer::class)
    val startDate: KotlinxLocalDate,

    val subdivisions: List<SubdivisionReferenceDto>? = null,
    val groups: List<GroupReferenceDto>? = null,
    val temporalScope: TemporalScopeDto? = null,
    val type: HolidayTypeDto
)

@Serializable
internal data class HolidayByDateDto(
    val comment: List<LocalizedTextDto>? = null,
    val country: CountryReferenceDto,
    val groups: List<GroupReferenceDto>? = null,
    val id: String,
    val name: List<LocalizedTextDto>,
    val nationwide: Boolean,
    val regionalScope: RegionalScopeDto? = null,
    val subdivisions: List<SubdivisionReferenceDto>? = null,
    val tags: HolidayTagsDto? = null,
    val temporalScope: TemporalScopeDto? = null,
    val type: HolidayTypeDto
)

@Serializable
internal data class StatisticsDto(
    @Serializable(with = LocalDateSerializer::class)
    val youngestStartDate: KotlinxLocalDate,

    @Serializable(with = LocalDateSerializer::class)
    val oldestStartDate: KotlinxLocalDate
)

@Serializable
internal data class ProblemDetailsDto(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val instance: String? = null
)

// Mappers from DTO to domain models

internal fun LocalizedTextDto.toDomain() = LocalizedText(language, text)

internal fun CountryReferenceDto.toDomain() = CountryReference(isoCode)

internal fun SubdivisionReferenceDto.toDomain() = SubdivisionReference(code, shortName)

internal fun GroupReferenceDto.toDomain() = GroupReference(code, shortName)

internal fun CountryDto.toDomain() = Country(
    isoCode = isoCode,
    name = name.map { it.toDomain() },
    officialLanguages = officialLanguages
)

internal fun LanguageDto.toDomain() = Language(
    isoCode = isoCode,
    name = name.map { it.toDomain() }
)

internal fun GroupDto.toDomain(): Group = Group(
    category = category.map { it.toDomain() },
    children = children?.map { it.toDomain() },
    code = code,
    comment = comment?.map { it.toDomain() },
    name = name.map { it.toDomain() },
    shortName = shortName,
    subdivisions = subdivisions?.map { it.toDomain() }
)

internal fun SubdivisionDto.toDomain(): Subdivision = Subdivision(
    category = category.map { it.toDomain() },
    children = children?.map { it.toDomain() },
    code = code,
    comment = comment?.map { it.toDomain() },
    groups = groups?.map { it.toDomain() },
    isoCode = isoCode,
    name = name.map { it.toDomain() },
    officialLanguages = officialLanguages,
    shortName = shortName
)

internal fun HolidayTagsDto.toDomain() = when (this) {
    HolidayTagsDto.RECOMMENDED -> HolidayTags.RECOMMENDED
    HolidayTagsDto.PROVISIONAL -> HolidayTags.PROVISIONAL
    HolidayTagsDto.ONE_TIME -> HolidayTags.ONE_TIME
    HolidayTagsDto.EXCEPTION -> HolidayTags.EXCEPTION
}

internal fun HolidayTypeDto.toDomain() = when (this) {
    HolidayTypeDto.PUBLIC -> HolidayType.PUBLIC
    HolidayTypeDto.BANK -> HolidayType.BANK
    HolidayTypeDto.OPTIONAL -> HolidayType.OPTIONAL
    HolidayTypeDto.SCHOOL -> HolidayType.SCHOOL
    HolidayTypeDto.BACK_TO_SCHOOL -> HolidayType.BACK_TO_SCHOOL
    HolidayTypeDto.END_OF_LESSONS -> HolidayType.END_OF_LESSONS
}

internal fun RegionalScopeDto.toDomain() = when (this) {
    RegionalScopeDto.NATIONAL -> RegionalScope.NATIONAL
    RegionalScopeDto.REGIONAL -> RegionalScope.REGIONAL
    RegionalScopeDto.LOCAL -> RegionalScope.LOCAL
}

internal fun TemporalScopeDto.toDomain() = when (this) {
    TemporalScopeDto.FULL_DAY -> TemporalScope.FULL_DAY
    TemporalScopeDto.HALF_DAY -> TemporalScope.HALF_DAY
}

internal fun HolidayDto.toDomain() = Holiday(
    comment = comment?.map { it.toDomain() },
    endDate = endDate.toJava(),
    id = id,
    name = name.map { it.toDomain() },
    nationwide = nationwide,
    regionalScope = regionalScope?.toDomain(),
    startDate = startDate.toJava(),
    subdivisions = subdivisions?.map { it.toDomain() },
    groups = groups?.map { it.toDomain() },
    temporalScope = temporalScope?.toDomain(),
    type = type.toDomain()
)

internal fun HolidayByDateDto.toDomain() = HolidayByDate(
    comment = comment?.map { it.toDomain() },
    country = country.toDomain(),
    groups = groups?.map { it.toDomain() },
    id = id,
    name = name.map { it.toDomain() },
    nationwide = nationwide,
    regionalScope = regionalScope?.toDomain(),
    subdivisions = subdivisions?.map { it.toDomain() },
    tags = tags?.toDomain(),
    temporalScope = temporalScope?.toDomain(),
    type = type.toDomain()
)

internal fun StatisticsDto.toDomain() = Statistics(
    youngestStartDate = youngestStartDate.toJava(),
    oldestStartDate = oldestStartDate.toJava()
)

internal fun ProblemDetailsDto.toDomain() = ProblemDetails(
    type = type,
    title = title,
    status = status,
    detail = detail,
    instance = instance
)

