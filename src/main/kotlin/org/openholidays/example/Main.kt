package org.openholidays.example
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.openholidays.OpenHolidaysClient

/**
 * Example demonstrating the usage of the OpenHolidaysClient.
 *
 * This example shows how to:
 * 1. Create a client instance
 * 2. Retrieve supported countries with localized names
 * 3. Query public holidays for a specific country and date range
 * 4. Extract localized holiday names from the response
 * 5. Properly close the client to release resources
 */
fun main() = runBlocking {
    // Create a new client instance with default configuration
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
        holidays.forEach { h ->
            val deName = h.name.firstOrNull { n -> n.language.equals("DE", ignoreCase = true) }?.text
            println("${h.startDate} – ${deName ?: h.name.firstOrNull()?.text}")
        }
    } finally {
        // Always close the client to release resources
        client.close()
    }
}
