package com.squareup.barber.examples

import com.squareup.barber.DocumentSpec
import com.squareup.barber.HtmlString

// TODO: this does not belong in main
/**
 * An example DocumentSpec
 */
data class TransactionalEmailDocumentSpec(
  val subject: String,
  val headline: HtmlString,
  val short_description: String,
  val primary_button: String?,
  val primary_button_url: String?,
  val secondary_button: String?,
  val secondary_button_url: String?
) : DocumentSpec