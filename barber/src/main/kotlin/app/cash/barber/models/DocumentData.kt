package app.cash.barber.models

import app.cash.barber.models.BarberSignature.Companion.getBarberSignature
import app.cash.barber.models.TemplateToken.Companion.getTemplateToken
import app.cash.protos.barber.api.BarberSignature.Type
import app.cash.protos.barber.api.DocumentData
import com.squareup.wire.Instant
import java.time.Duration
import kotlin.reflect.full.memberProperties

/**
 * This is a schema that specifies the input values for a DocumentTemplate template.
 *
 * Examples:
 * data class RecipientReceipt(
 *  val amount: Money,
 *  val sender: String,
 *  val depositExpectedAt: Instant,
 *  val cancelUrl: HttpUrl
 * ) : DocumentData
 *
 * data class AutoAddCashFailed(
 *  val amount: Money,
 *  val reason: String
 * ) : DocumentData
 *
 * data class PaymentLateHelpArticle(
 *  val amount: Money,
 *  val displayId: String,
 *  val depositExpectedAt: Instant
 * ) : DocumentData
 *
 * Instances of DocumentData are used to fill the templates in a DocumentTemplate and produce a Document object.
 *
 * A Barber.render function consumes the Document object and renders the final document, SMS, email, or article.
 *
 * Copy models do not have a locale.
 */
interface DocumentData {
  fun toProto() = app.cash.protos.barber.api.DocumentData(
      template_token = this::class.getTemplateToken().token,
      fields = this.toDocumentDataFields()
  )

  /** Provider interoperability with DocumentData proto */
  private fun toDocumentDataFields() = getBarberSignature().fields.map { (key, type) ->
    when (type) {
      Type.STRING -> getDocumentDataValue(key)?.let {
        DocumentData.Field(key = key,
            value_string = it as String
        )
      } ?: getNullDocumentDataField(key, Type.STRING)
      Type.LONG -> getDocumentDataValue(key)?.let {
        DocumentData.Field(
            key = key,
            value_long = it as Long
        )
      } ?: getNullDocumentDataField(key, Type.LONG)
      Type.DURATION -> getDocumentDataValue(key)?.let {
        DocumentData.Field(
            key = key,
            value_duration = it as Duration
        )
      } ?: getNullDocumentDataField(key, Type.DURATION)
      Type.INSTANT -> getDocumentDataValue(key)?.let {
        DocumentData.Field(
            key = key,
            value_instant = it as Instant
        )
      } ?: getNullDocumentDataField(key, Type.INSTANT)
      Type.TYPE_DO_NOT_USE -> throw IllegalArgumentException("Can not parse TYPE_DO_NOT_USE")
    }
  }

  private fun getNullDocumentDataField(key: String, type: Type) = DocumentData.Field(
      key = key,
      value_null_type = type
  )

  /**
   * Lookup actual values for construction of proto using the dot separated full path from the
   * Barber Signature field keys
   */
  private fun getDocumentDataValue(key: String): Any? = key
      .split('.')
      .foldIndexed(this) { index, acc: Any?, segment ->
        getValue(acc as Any, segment)?.let {
          if (it::class.isData || index == key.split('.').lastIndex) {
            // Only recurse for data classes or return the final value
            // Do not recurse on non-data classes
            it
          } else {
            null
          }
        }
      }

  private fun <T : Any> getValue(member: T, key: String): Any? {
    require(!key.contains('.')) { "key can not be a dot segmented path" }
    val memberProperties = member::class.memberProperties
    val kProperty = memberProperties.find { it.name == key }
    return kProperty?.getter?.call(member)
  }
}