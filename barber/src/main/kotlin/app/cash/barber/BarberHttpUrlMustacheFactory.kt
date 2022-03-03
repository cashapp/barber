package app.cash.barber

import app.cash.barber.models.BarberFieldEncoding.STRING_HTTPURL
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.MustacheException
import com.github.mustachejava.util.HtmlEscaper
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.Writer

/**
 * Mustache Factory that handles [STRING_HTTPURL] fields and doesn't apply the HTML escaping
 *  present in [DefaultMustacheFactory] if it's a valid OkHttp.HttpUrl
 */
class BarberHttpUrlMustacheFactory : DefaultMustacheFactory() {
  override fun encode(value: String?, writer: Writer?) {
    writer?.let { w ->
      value?.let { v ->
        try {
          // Escape any non-URL valid characters if the value is a valid URL
          w.write(v.toHttpUrl().toString())
        } catch (e: IllegalArgumentException) {
          // Else, assume that this is not a valid URL
          // Escape the entire string and don't throw an exception
          HtmlEscaper.escape(v, w)
        }
      } ?: throw MustacheException("Null Writer. Failed to encode value: $value")
    }
  }
}
