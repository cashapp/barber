package app.cash.barber

import app.cash.barber.models.BarberFieldEncoding
import com.github.mustachejava.DefaultMustacheFactory

object BarberMustacheFactoryProvider {
  private val defaultMustacheFactory = DefaultMustacheFactory()
  private val barberPlaintextMustacheFactory = BarberPlaintextMustacheFactory()
  fun get(encoding: BarberFieldEncoding = BarberFieldEncoding.STRING_HTML) = when (encoding) {
    BarberFieldEncoding.STRING_HTML -> defaultMustacheFactory
    BarberFieldEncoding.STRING_PLAINTEXT -> barberPlaintextMustacheFactory
  }
}
