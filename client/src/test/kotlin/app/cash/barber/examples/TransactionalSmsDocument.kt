package app.cash.barber.examples

import app.cash.barber.models.Document

/**
 * An example Document for Sms messages
 */
data class TransactionalSmsDocument(
  val sms_body: String
) : Document