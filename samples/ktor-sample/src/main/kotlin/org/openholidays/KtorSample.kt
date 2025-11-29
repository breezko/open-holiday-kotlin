package org.openholidays

import kotlinx.coroutines.runBlocking
import org.openholidays.ktor.KtorHolidaysClient
import java.time.LocalDate

/**
 * Simple console sample that uses the published Ktor client.
 *
 * Ensure you have executed `./gradlew publishToMavenLocal` in the root project
 * so `mavenLocal()` can resolve the library coordinates.
 */
fun main(): Unit = runBlocking {
    val client = KtorHolidaysClient.create()
    client.use {
        val holidays = it.getPublicHolidays(
            countryIsoCode = "DE",
            validFrom = LocalDate.of(2025, 1, 1),
            validTo = LocalDate.of(2025, 12, 31),
            languageIsoCode = "EN"
        )

        println("Found ${holidays.size} holidays in Germany for 2025")
        holidays.take(5).forEach { holiday ->
            val englishName = holiday.name.firstOrNull { text -> text.language.equals("EN", true) }?.text
            println("- ${englishName ?: holiday.name.firstOrNull()?.text} (${holiday.startDate} - ${holiday.endDate})")
        }
    }
}
