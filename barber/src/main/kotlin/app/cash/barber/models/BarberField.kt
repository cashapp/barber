package app.cash.barber.models

import com.github.mustachejava.MustacheFactory

/**
 * Document fields annotated will be rendered with a [MustacheFactory] corresponding to the
 *   [BarberFieldEncoding].
 */
annotation class BarberField(val encoding: BarberFieldEncoding = BarberFieldEncoding.STRING_HTML)
