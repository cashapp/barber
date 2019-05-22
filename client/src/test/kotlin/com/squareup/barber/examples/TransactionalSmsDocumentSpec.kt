package com.squareup.barber.examples

import com.squareup.barber.DocumentSpec
import com.squareup.barber.HtmlString

// TODO: this does not belong in main
/**
 * An example DocumentSpec for Sms messages
 */
data class TransactionalSmsDocumentSpec(
  val sms_body: String
) : DocumentSpec