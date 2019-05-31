package com.squareup.barber.models

/**
 * A {language code}-{country code} Locale
 *
 * Example
 *  en-US: English US
 *  en-CA: English Canada
 *  fr-FR: French France
 *  fr-CA: French Canada
 */
data class Locale(
  /** Like en */
  // TODO migrate to ENUM instead of String
  val languageCode: String,
  /** Like US */
  // TODO migrate to ENUM instead of String
  val countryCode: String
) {
  // TODO populate with existing ones
  companion object {
    val EN_US = Locale("en", "US")
  }

  //TODO add resolve algorithm with fallbacks
}