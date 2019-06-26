package com.squareup.barber

import com.squareup.barber.LocaleResolver.Companion.resolveEntry
import com.squareup.barber.models.Locale
import com.squareup.barber.models.Locale.Companion.EN_CA
import com.squareup.barber.models.Locale.Companion.EN_GB
import com.squareup.barber.models.Locale.Companion.EN_US
import com.squareup.barber.models.Locale.Companion.ES_US
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MatchOrFirstLocaleResolverTest {
  private lateinit var localeResolver: LocaleResolver

  @BeforeEach
  fun before() {
    localeResolver = MatchOrFirstLocaleResolver()
  }

  @Test
  fun `Requested locale is installed`() {
    val localeEntries =
        mapOf(EN_US to "English US", EN_CA to "English Canada", EN_GB to "English Great Britain")
    assertEquals("English Canada", localeEntries.resolveEntry(localeResolver, EN_CA))
  }

  @Test
  fun `First locale option is returned when requested is not installed`() {
    val localeEntries =
        mapOf(EN_US to "English US", EN_GB to "English Great Britain")
    assertEquals("English US", localeEntries.resolveEntry(localeResolver, EN_CA))
  }

  @Test
  fun `Error if there are no installed locales`() {
    val localeEntries: Map<Locale, String> = mapOf()
    assertFailsWith<NoSuchElementException> {
      localeEntries.resolveEntry(localeResolver, ES_US)
    }
  }
}