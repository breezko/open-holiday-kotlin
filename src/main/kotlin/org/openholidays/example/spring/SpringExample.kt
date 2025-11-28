package org.openholidays.example.spring

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.openholidays.HolidaysClient
import org.openholidays.spring.SpringWebClientHolidaysClient
import org.springframework.web.reactive.function.client.WebClient

/**
 * Example demonstrating the usage of the Spring WebClient-based HolidaysClient.
 *
 * This example shows how to:
 * 1. Create a Spring WebClient-based client instance
 * 2. Use the same HolidaysClient interface as with Ktor
 * 3. Retrieve and display holiday data
 *
 * For Spring Boot applications, you can configure the client as a Bean:
 *
 * ```kotlin
 * @Configuration
 * class HolidaysConfig {
 *     @Bean
 *     fun holidaysClient(webClientBuilder: WebClient.Builder): HolidaysClient {
 *         return SpringWebClientHolidaysClient.create(
 *             webClientBuilder = webClientBuilder
 *         )
 *     }
 * }
 *
 * @Service
 * class HolidayService(private val holidaysClient: HolidaysClient) {
 *     suspend fun getHolidaysForCountry(country: String, year: Int): List<Holiday> {
 *         return holidaysClient.getPublicHolidays(
 *             countryIsoCode = country,
 *             validFrom = LocalDate(year, 1, 1),
 *             validTo = LocalDate(year, 12, 31)
 *         )
 *     }
 * }
 *
 * @RestController
 * @RequestMapping("/api/holidays")
 * class HolidayController(private val holidayService: HolidayService) {
 *     @GetMapping("/{country}")
 *     suspend fun getHolidays(
 *         @PathVariable country: String,
 *         @RequestParam(defaultValue = "2025") year: Int
 *     ): List<Holiday> {
 *         return holidayService.getHolidaysForCountry(country, year)
 *     }
 * }
 * ```
 */
fun main() = runBlocking {
    // Create a Spring WebClient-based client
    val client: HolidaysClient = SpringWebClientHolidaysClient.create()

    try {
        // List supported countries (English names)
        val countries = client.getCountries(languageIsoCode = "EN")
        println("First 5 countries:")
        countries.take(5).forEach {
            val enName = it.name.firstOrNull { n -> n.language.equals("EN", ignoreCase = true) }?.text
            println("${it.isoCode} - $enName")
        }

        // Get French public holidays for 2025
        val holidays = client.getPublicHolidays(
            countryIsoCode = "FR",
            validFrom = LocalDate.parse("2025-01-01"),
            validTo = LocalDate.parse("2025-12-31"),
            languageIsoCode = "FR"
        )

        println("\nFR public holidays in 2025:")
        holidays.forEach { h ->
            val frName = h.name.firstOrNull { n -> n.language.equals("FR", ignoreCase = true) }?.text
            println("${h.startDate} – ${frName ?: h.name.firstOrNull()?.text}")
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }
}

