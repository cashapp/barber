package app.cash.barber.examples

import app.cash.barber.models.BarberField
import app.cash.barber.models.BarberFieldEncoding
import app.cash.barber.models.Document

/**
 * An example Document that shadows field names of [TransactionalSmsDocument]
 */
data class ShadowPlaintextDocument(
  @BarberField(encoding = BarberFieldEncoding.STRING_PLAINTEXT)
  val plaintext_field: String
) : Document
