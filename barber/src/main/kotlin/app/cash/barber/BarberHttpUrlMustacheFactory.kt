package app.cash.barber

import app.cash.barber.models.BarberFieldEncoding.STRING_HTTPURL
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.MustacheException
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.Writer

/**
 * Mustache Factory that handles [STRING_HTTPURL] fields and doesn't apply the HTML escaping
 *  present in [DefaultMustacheFactory]
 */
class BarberHttpUrlMustacheFactory : DefaultMustacheFactory() {
  override fun encode(value: String?, writer: Writer?) {
    value?.let { writer?.write(it.toHttpUrl().toString()) }
      ?: throw MustacheException("Null Writer. Failed to encode value: $value")
  }
}
