package app.cash.barber.locale

import app.cash.barber.BarberException

internal object MatchOrFirstLocaleResolver : LocaleResolver {
  override fun resolve(locale: Locale, options: Set<Locale>, templateToken: String): Locale = when {
    options.isEmpty() -> {
      throw BarberException(listOf("Unable to resolve a locale for [locale=${locale}] from 0 options [templateToken=${templateToken}]."))
    }
    options.contains(locale) -> {
      locale
    }
    else -> {
      options.first()
    }
  }
}
