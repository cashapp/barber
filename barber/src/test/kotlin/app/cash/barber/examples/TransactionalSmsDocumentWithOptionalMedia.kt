package app.cash.barber.examples

import app.cash.barber.models.BarberField
import app.cash.barber.models.BarberFieldEncoding
import app.cash.barber.models.Document

/**
 * Used to illustrate compatibility with optional fields,
 * e.g. if we wanted to add an optional field to the existing [TransactionalSmsDocument]
 * without causing breaking changes for its existing Documents.
 */
data class TransactionalSmsDocumentWithOptionalMedia(
  val sms_body: String,
  @BarberField(encoding = BarberFieldEncoding.STRING_PLAINTEXT)
  // Example addition where we may want to add the ability to send media over MMS
  val sms_mediaUrl: String? = null,
) : Document
