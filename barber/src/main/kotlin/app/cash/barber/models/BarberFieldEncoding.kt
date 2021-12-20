package app.cash.barber.models

/**
 * Used in [BarberField] annotation on Document fields to specify the encoding to render using.
 */
enum class BarberFieldEncoding {
  STRING_HTML, STRING_PLAINTEXT, STRING_HTTPURL
}
