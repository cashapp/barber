package app.cash.barber.models

/**
 * Allow clients to declare on DocumentData or Document fields a specific encoding that the value
 *   is to be rendered and handled as. By default, fields are treated as [STRING_HTML].
 */
enum class BarberFieldEncoding {
  STRING_HTML, STRING_PLAINTEXT
}
