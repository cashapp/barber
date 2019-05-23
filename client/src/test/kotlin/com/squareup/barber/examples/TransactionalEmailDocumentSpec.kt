package com.squareup.barber.examples

import com.squareup.barber.models.DocumentSpec

// TODO: this does not belong in main
// TODO: support HTMLString type
/**
 * An example DocumentSpec
 */
data class TransactionalEmailDocumentSpec(
  val subject: String,
  val headline: String,
  val short_description: String,
  val primary_button: String?,
  val primary_button_url: String?,
  val secondary_button: String?,
  val secondary_button_url: String?
) : DocumentSpec