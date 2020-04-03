package app.cash.barber.models

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.util.HtmlEscaper

/**
 * Document fields annotated as such will NOT have respective DocumentTemplate fields escaped using
 *   the [HtmlEscaper] in [DefaultMustacheFactory]. Such fields should only include input that is
 *   trusted, free from any invalid characters or potential injection attacks, and targets non-HTML.
 */
annotation class BarberField(val encoding: BarberFieldEncoding = BarberFieldEncoding.STRING_HTML)

/**
 * Allow clients to declare on DocumentData or Document fields a specific encoding that the value
 *   is to be rendered and handled as. By default, fields are treated as [STRING_HTML].
 */
enum class BarberFieldEncoding {
  STRING_HTML, STRING_PLAINTEXT
}