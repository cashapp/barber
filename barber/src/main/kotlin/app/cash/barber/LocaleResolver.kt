package app.cash.barber

import app.cash.barber.models.Locale

/**
 * Chooses which locale to use given an inventory of available templates. This class is responsible
 * for making difficult policy decisions around which text to show which customers.
 *
 * For example, it may need to decide how to satisfy a request for es-US (Spanish in the USA) given
 * templates for es-ES (Spanish in Spain) and en-US (English in the USA).
 */
interface LocaleResolver {
  fun resolve(locale: Locale, options: Set<Locale>): Locale

  fun <T> resolve(locale: Locale, map: Map<Locale, T?>): T {
    return map[resolve(locale, map.keys)] ?: if (map.isEmpty()) {
      // Usage in Barber prevents the empty case from happening
      throw BarberException(errors = listOf("Can not resolve entry of an empty Map."))
    } else {
      // LocaleResolver has not respected the contract that they must resolve a valid key in Map
      throw BarberException(errors = listOf("""
          |Resolved entry is not valid key in Map.
          |LocaleResolver: ${this::class}
          |Locale: $locale
          |Resolved Locale: ${resolve(locale, map.keys)}
          """.trimMargin()))
    }
  }
}
