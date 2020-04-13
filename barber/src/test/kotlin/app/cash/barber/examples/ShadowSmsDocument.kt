package app.cash.barber.examples

import app.cash.barber.models.Document

/**
 * An example Document that shadows field names of [TransactionalSmsDocument]
 */
data class ShadowSmsDocument(
  val sms_body: String
) : Document
