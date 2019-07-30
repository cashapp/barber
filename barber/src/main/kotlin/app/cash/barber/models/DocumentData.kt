package app.cash.barber.models

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
interface DocumentData
