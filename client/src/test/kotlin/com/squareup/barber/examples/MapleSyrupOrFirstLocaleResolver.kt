package com.squareup.barber.examples

import com.squareup.barber.LocaleResolver
import com.squareup.barber.models.Locale
import com.squareup.barber.models.Locale.Companion.EN_CA

class MapleSyrupOrFirstLocaleResolver : LocaleResolver {
  override fun resolve(locale: Locale, options: Set<Locale>): Locale =
    if (options.contains(EN_CA)) {
      EN_CA
    } else {
      options.first()
    }
}