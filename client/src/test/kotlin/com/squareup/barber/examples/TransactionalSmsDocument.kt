package com.squareup.barber.examples

import com.squareup.barber.models.Document

/**
 * An example Document for Sms messages
 */
data class TransactionalSmsDocument(
  val sms_body: String
) : Document