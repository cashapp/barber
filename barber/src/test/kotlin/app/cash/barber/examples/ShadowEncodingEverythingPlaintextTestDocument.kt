package app.cash.barber.examples

import app.cash.barber.models.BarberField
import app.cash.barber.models.BarberFieldEncoding
import app.cash.barber.models.Document

/**
 * Test document to exercise shadowed field names with different [BarberFieldEncoding]
 */
data class ShadowEncodingEverythingPlaintextTestDocument(
  @BarberField(encoding = BarberFieldEncoding.STRING_PLAINTEXT)
  val no_annotation_field: String,
  @BarberField(encoding = BarberFieldEncoding.STRING_PLAINTEXT)
  val default_field: String,
  @BarberField(encoding = BarberFieldEncoding.STRING_PLAINTEXT)
  val html_field: String,
  @BarberField(encoding = BarberFieldEncoding.STRING_PLAINTEXT)
  val plaintext_field: String,
  @BarberField(encoding = BarberFieldEncoding.STRING_PLAINTEXT)
  val non_shadow_field: String
) : Document
