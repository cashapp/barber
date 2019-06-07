package com.squareup.barber.models

/**
 * Container for locale string
 * Also has aliases for easier locale use in code
 *
 * Examples
 *  en-US: English US
 *  en-CA: English Canada
 *  fr-FR: French France
 *  fr-CA: French Canada
 */
data class Locale(val locale: String) {
  companion object {
    val EN_CA = Locale("en-CA")
    val EN_GB = Locale("en-GB")
    val EN_US = Locale("en-US")

    val FR_CA = Locale("fr-CA")
    val FR_FR = Locale("fr-FR")

    val ES_SP = Locale("es-SP")
    val ES_US = Locale("es-US")
  }
}