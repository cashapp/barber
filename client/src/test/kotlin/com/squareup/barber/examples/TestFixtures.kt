package com.squareup.barber.examples

import com.squareup.barber.models.DocumentTemplate
import com.squareup.barber.models.Locale
import com.squareup.barber.models.Locale.Companion.EN_CA
import com.squareup.barber.models.Locale.Companion.EN_GB

val recipientReceiptSmsDocumentTemplateEN_US = DocumentTemplate(
  fields = mapOf(
    "subject" to "{{sender}} sent you {{amount}}",
    "headline" to "You received {{amount}}",
    "short_description" to "You’ve received a payment from {{sender}}! The money will be in your bank account " +
      "{{deposit_expected_at_casual}}.",
    "primary_button" to "Cancel this payment",
    "primary_button_url" to "{{cancelUrl}}",
    "sms_body" to "{{sender}} sent you {{amount}}"
  ),
  source = RecipientReceipt::class,
  targets = setOf(TransactionalSmsDocument::class),
  locale = Locale.EN_US
)

val recipientReceiptSmsDocumentTemplateEN_CA = recipientReceiptSmsDocumentTemplateEN_US.copy(
  fields = recipientReceiptSmsDocumentTemplateEN_US.fields.mapValues { it.value + " Eh?" },
  locale = EN_CA
)

val recipientReceiptSmsDocumentTemplateEN_GB = recipientReceiptSmsDocumentTemplateEN_US.copy(
  fields = recipientReceiptSmsDocumentTemplateEN_US.fields.mapValues { it.value + " The Queen approves." },
  locale = EN_GB
)