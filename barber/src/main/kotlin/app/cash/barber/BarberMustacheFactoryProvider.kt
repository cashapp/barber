package app.cash.barber

import app.cash.barber.models.BarberFieldEncoding
import app.cash.barber.models.Document
import com.github.mustachejava.DefaultMustacheFactory

/**
 * Provides a MustacheFactory depending on the [BarberFieldEncoding] of the [Document] field
 */
object BarberMustacheFactoryProvider {
  private val defaultMustacheFactory = DefaultMustacheFactory()
  private val barberPlaintextMustacheFactory = BarberPlaintextMustacheFactory()
  fun get(encoding: BarberFieldEncoding?) = when (encoding) {
    BarberFieldEncoding.STRING_PLAINTEXT -> barberPlaintextMustacheFactory
    BarberFieldEncoding.STRING_HTML, null -> defaultMustacheFactory
  }
}
