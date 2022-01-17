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
    value?.let {
      val schema = value.split("://").first()

      // Permit non https schemas to support deep links
      val forEncoding = "https://" + value.split("://").last()
      val encodedWithoutSchema = forEncoding.toHttpUrl().toString().removePrefix("https")

      writer?.write(schema + encodedWithoutSchema)
    } ?: throw MustacheException("Null Writer. Failed to encode value: $value")
  }
}
