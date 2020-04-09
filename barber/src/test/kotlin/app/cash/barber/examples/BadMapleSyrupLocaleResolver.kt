package app.cash.barber.examples

import app.cash.barber.LocaleResolver
import app.cash.barber.models.Locale
import app.cash.barber.models.Locale.Companion.EN_CA

class BadMapleSyrupLocaleResolver : LocaleResolver {
  override fun resolve(locale: Locale, options: Set<Locale>): Locale {
    return EN_CA
  }
}
