package com.squareup.barber.examples

import com.squareup.barber.LocaleResolver
import com.squareup.barber.models.Locale
import com.squareup.barber.models.Locale.Companion.EN_CA

class BadMapleSyrupLocaleResolver : LocaleResolver {
  override fun resolve(locale: Locale, options: Set<Locale>): Locale {
    return EN_CA
  }
}