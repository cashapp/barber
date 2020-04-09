package app.cash.barber

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.MustacheException
import java.io.Writer

class BarberPlaintextMustacheFactory : DefaultMustacheFactory() {
  override fun encode(value: String?, writer: Writer?) {
    writer?.write(value) ?: throw MustacheException("Null Writer. Failed to encode value: $value")
  }
}
