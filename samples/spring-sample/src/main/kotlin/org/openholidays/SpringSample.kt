package org.openholidays

import kotlinx.coroutines.runBlocking
import org.openholidays.spring.SpringWebClientHolidaysClient
import java.time.LocalDate

/**
 * Spring WebClient sample that exercises the published Spring client.
 *
 * Publish the library to your local Maven repo first via `./gradlew publishToMavenLocal`
 * and then run this sample with `../gradlew -p samples/spring-sample run`.
 */
fun main(): Unit = runBlocking {
    val client = SpringWebClientHolidaysClient.create()

    val holidays = client.getSchoolHolidays(
        countryIsoCode = "AT",
        validFrom = LocalDate.of(2025, 1, 1),
        validTo = LocalDate.of(2025, 12, 31),
        languageIsoCode = "EN"
    )

    println("Found ${holidays.size} Austrian school holidays in 2025")
    holidays.take(5).forEach { holiday ->
        val englishName = holiday.name.firstOrNull { text -> text.language.equals("EN", true) }?.text
        println("- ${englishName ?: holiday.name.firstOrNull()?.text} (${holiday.startDate} - ${holiday.endDate})")
    }
}
