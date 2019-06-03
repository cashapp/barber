package com.squareup.barber

import com.squareup.barber.models.Locale

class MatchOrFirstLocaleResolver : LocaleResolver {
  override fun resolve(locale: Locale, options: Set<Locale>): Locale =
    if (options.contains(locale)) {
      locale
    } else {
      options.first()
    }
}