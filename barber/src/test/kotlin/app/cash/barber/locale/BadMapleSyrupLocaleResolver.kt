package app.cash.barber.locale

import app.cash.barber.locale.Locale.Companion.EN_CA

class BadMapleSyrupLocaleResolver : LocaleResolver {
  override fun resolve(locale: Locale, options: Set<Locale>, templateToken: String): Locale {
    return EN_CA
  }
}
