package com.squareup.barber

import com.squareup.barber.models.Locale

interface LocaleResolver {
  /**
   * @return a [Locale] from the given [options]
   * @param [options] must be valid keys
   */
  fun resolve(locale: Locale, options: Set<Locale>): Locale

  companion object {
    /**
     * @return entry of a [Locale] keyed Map using [LocaleResolver]
     */
    fun <T> Map<Locale, T?>.resolveEntry(
      localeResolver: LocaleResolver,
      locale: Locale
    ) = this[localeResolver.resolve(locale, keys)] ?: if (this.isEmpty()) {
      throw BarberException(problems = listOf("Can not resolve entry of an empty Map."))
    } else {
      throw BarberException(problems = listOf("""
        |Resolved entry is not valid key in Map.
        |LocaleResolver: ${localeResolver::class}
        |Locale: $locale
        |Resolved Locale: ${localeResolver.resolve(locale, keys)}
        """.trimMargin()))
    }
  }
}