package app.cash.barber.locale

import app.cash.barber.BarberException
import app.cash.barber.locale.Locale.Companion.EN_CA
import app.cash.barber.locale.Locale.Companion.EN_GB
import app.cash.barber.locale.Locale.Companion.EN_US
import app.cash.barber.locale.Locale.Companion.ES_US
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MatchOrFirstLocaleResolverTest {
  private lateinit var localeResolver: LocaleResolver

  @BeforeEach
  fun before() {
    localeResolver = MatchOrFirstLocaleResolver
  }

  @Test
  fun `Requested locale is installed`() {
    val localeEntries =
        mapOf(EN_US to "English US", EN_CA to "English Canada", EN_GB to "English Great Britain")
    assertEquals("English Canada", localeResolver.resolve(EN_CA, localeEntries, "alphaBravo"))
  }

  @Test
  fun `First locale option is returned when requested is not installed`() {
    val localeEntries =
        mapOf(EN_US to "English US", EN_GB to "English Great Britain")
    assertEquals("English US", localeResolver.resolve(EN_CA, localeEntries,"alphaBravo"))
  }

  @Test
  fun `Error if there are no installed locales`() {
    val localeEntries: Map<Locale, String> = mapOf()
    val exception = assertFailsWith<BarberException> {
      localeResolver.resolve(ES_US, localeEntries, "alphaBravo")
    }
    assertEquals("""
    |Errors
    |1) Unable to resolve a locale for [locale=[Locale=es-US]] from 0 options [templateToken=alphaBravo].
    |
    """.trimMargin(), exception.toString())
  }
}
