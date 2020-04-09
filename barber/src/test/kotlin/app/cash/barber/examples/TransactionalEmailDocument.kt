package app.cash.barber.examples

import app.cash.barber.models.Document

// TODO: support HTMLString type
/**
 * An example Document
 */
data class TransactionalEmailDocument(
  val subject: String,
  val headline: String,
  val short_description: String,
  val primary_button: String?,
  val primary_button_url: String?,
  val secondary_button: String?,
  val secondary_button_url: String?
) : Document
