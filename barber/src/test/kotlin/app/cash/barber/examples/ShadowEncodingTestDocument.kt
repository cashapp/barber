package app.cash.barber.examples

import app.cash.barber.models.BarberField
import app.cash.barber.models.BarberFieldEncoding
import app.cash.barber.models.Document

/**
 * Test document to exercise shadowed field names
 */
data class ShadowEncodingTestDocument(
  val no_annotation_field: String,
  @BarberField()
  val default_field: String,
  @BarberField(encoding = BarberFieldEncoding.STRING_HTML)
  val html_field: String,
  @BarberField(encoding = BarberFieldEncoding.STRING_PLAINTEXT)
  val plaintext_field: String,
  val non_shadow_field: String
) : Document
