package app.cash.barber.examples

import app.cash.barber.LocaleResolver
import app.cash.barber.models.Locale
import app.cash.barber.models.Locale.Companion.EN_CA

class MapleSyrupOrFirstLocaleResolver : LocaleResolver {
  override fun resolve(locale: Locale, options: Set<Locale>): Locale =
    if (options.contains(EN_CA)) {
      EN_CA
    } else {
      options.first()
    }
}
