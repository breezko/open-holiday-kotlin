package org.openholidays.example
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.openholidays.OpenHolidaysClient

fun main() = runBlocking {
    val client = OpenHolidaysClient.create()

    try {
        // List supported countries (English names)
        val countries = client.getCountries(languageIsoCode = "EN")
        println("First 5 countries:")
        countries.take(5).forEach {
            val enName = it.name.firstOrNull { n -> n.language.equals("EN", ignoreCase = true) }?.text
            println("${it.isoCode} - $enName")
        }

        // Get German public holidays for 2025
        val holidays = client.getPublicHolidays(
            countryIsoCode = "DE",
            validFrom = LocalDate.parse("2025-01-01"),
            validTo = LocalDate.parse("2025-12-31"),
            languageIsoCode = "DE"

        )

        println("\nDE public holidays in 2025:")
        println(holidays)
        holidays.forEach { h ->
            val deName = h.name.firstOrNull { n -> n.language.equals("DE", ignoreCase = true) }?.text
            println("${h.startDate} – ${deName ?: h.name.firstOrNull()?.text}")
        }
    } finally {
        client.close()
    }
}
