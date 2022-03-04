package app.cash.barber

import app.cash.barber.models.BarberFieldEncoding.STRING_PLAINTEXT
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.MustacheException
import java.io.Writer

/**
 * Mustache Factory that handles [STRING_PLAINTEXT] fields and doesn't apply the HTML escaping
 *  present in [DefaultMustacheFactory]
 */
class BarberPlaintextMustacheFactory : DefaultMustacheFactory() {
  override fun encode(value: String?, writer: Writer?) {
    value?.let {
      writer?.write(it)
        ?: throw MustacheException("Null Writer. Failed to encode value: $value")
    }
  }
}
