package app.cash.barber

import app.cash.barber.models.Locale

class MatchOrFirstLocaleResolver : LocaleResolver {
  override fun resolve(locale: Locale, options: Set<Locale>): Locale =
    if (options.contains(locale)) {
      locale
    } else {
      options.first()
    }
}