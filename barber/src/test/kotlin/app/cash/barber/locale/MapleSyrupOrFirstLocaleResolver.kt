package app.cash.barber.locale

import app.cash.barber.locale.Locale.Companion.EN_CA

class MapleSyrupOrFirstLocaleResolver : LocaleResolver {
  override fun resolve(locale: Locale, options: Set<Locale>, templateToken: String): Locale =
      if (options.contains(EN_CA)) {
        EN_CA
      } else {
        options.first()
      }
}
